package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Resource;
import org.optaplanner.examples.icon.domain.ResourceRequirement;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

/**
 * Validates feasibility requirements. Counts how many more resources would we need than we have capacity for.
 */
public class CapacityTracker {

    private int overused = 0;

    private final int periodCount;

    /**
     * Machine -> Period -> Resource
     */
    private final int[][][] resourceConsumptionInTime;
    private final int resourceCount;

    public CapacityTracker(final Schedule problem) {
        this.resourceCount = problem.getResourceCount();
        this.periodCount = 1440 / problem.getResolution();
        this.resourceConsumptionInTime = new int[problem.getMachines().size()][][];
    }

    public void add(final TaskAssignment a) {
        this.overused += this.process(a, true);
    }

    private int[][] getConsumptionsForMachine(final Machine m) {
        final int machineId = m.getId();
        final int[][] consumptionPerMachine = this.resourceConsumptionInTime[machineId];
        if (consumptionPerMachine == null) {
            /*
             * this array needs to have room for all resources in the
             * problem, but will only have occupied a few of them. however,
             * this is dramatically faster than having a properly sized
             * collection on which we put()/get() all the time.
             */
            return this.resourceConsumptionInTime[machineId] = new int[this.periodCount][];
        } else {
            return consumptionPerMachine;
        }
    }

    public int getOverusedCapacity() {
        return this.overused;
    }

    public void modify(final TaskAssignment a, final Period previousStartPeriod, final Period previousFinalPeriod) {
        final Machine m = a.getExecutor();
        final int previousStart = previousStartPeriod.getId();
        final int previousFinal = previousFinalPeriod.getId();
        final Period currentStartPeriod = a.getStartPeriod();
        final Period currentFinalPeriod = a.getFinalPeriod();
        final int currentStart = currentStartPeriod.getId();
        final int currentFinal = currentFinalPeriod.getId();

        int total = 0;
        final int periodsToRecalculate = Math.abs(previousStart - currentStart);
        final boolean differentialMakesSense = periodsToRecalculate < a.getTask().getDuration();
        if (previousStart > currentStart) {
            // task moves to the left
            final boolean hasOverlap = currentFinal >= previousStart;
            if (!hasOverlap || !differentialMakesSense) {
                // optimization makes no sense; just make a direct remove/add
                a.setStartPeriod(previousStartPeriod);
                this.remove(a);
                a.setStartPeriod(currentStartPeriod);
                this.add(a);
            } else {
                // there is overlap
                for (final ResourceRequirement rr : a.getTask().getConsumptions()) {
                    total += this.processRequirement(currentStart, previousStartPeriod.previous().getId(), m, rr, true);
                    total -= this.processRequirement(currentFinalPeriod.next().getId(), previousFinalPeriod.getId(), m, rr, false);
                }
            }
        } else if (currentStart > previousStart) {
            // task moves to the right
            final boolean hasOverlap = previousFinal >= currentStart;
            if (!hasOverlap || !differentialMakesSense) {
                // optimization makes no sense; just make a direct remove/add
                a.setStartPeriod(previousStartPeriod);
                this.remove(a);
                a.setStartPeriod(currentStartPeriod);
                this.add(a);
            } else {
                for (final ResourceRequirement rr : a.getTask().getConsumptions()) {
                    total -= this.processRequirement(previousStart, currentStartPeriod.previous().getId(), m, rr, false);
                    total += this.processRequirement(previousFinalPeriod.next().getId(), currentFinalPeriod.getId(), m, rr, true);
                }
            }
        }
        this.overused += total;
    }

    private int process(final TaskAssignment a, final boolean isAdding) {
        final int startDate = a.getStartPeriod().getId();
        final int dueDate = a.getFinalPeriod().getId();
        final Machine m = a.getExecutor();
        int total = 0;
        for (final ResourceRequirement rr : a.getTask().getConsumptions()) {
            total += this.processRequirement(startDate, dueDate, m, rr, isAdding);
        }
        return total;
    }

    private int processRequirement(final int startDate, final int dueDate, final Machine m, final ResourceRequirement rr, final boolean isAdding) {
        final Resource resource = rr.getResource();
        final int requirement = rr.getRequirement();
        final int resourceId = resource.getId();
        final int capacity = m.getCapacity(rr.getResource()).getCapacity();
        final int[][] consumption = this.getConsumptionsForMachine(m);
        int total = 0;
        for (int time = startDate; time <= dueDate; time++) {
            total += this.processRequirementInTime(requirement, resourceId, capacity, consumption, time, isAdding);
        }
        return total;
    }

    private int processRequirementInTime(final int requirement, final int resourceId, final int capacity, final int[][] consumption, final int time, final boolean isAdding) {
        int[] totalUse = consumption[time];
        int currentUse = 0; // how much of the resource is being used at the given time
        if (totalUse == null) { // nothing has been consumed so far
            totalUse = new int[this.resourceCount];
            consumption[time] = totalUse;
        } else {
            currentUse = totalUse[resourceId];
        }
        return isAdding ?
                this.recalculateConsumptionOnAddition(totalUse, resourceId, currentUse, requirement, capacity) :
                    this.recalculateConsumptionOnRemoval(totalUse, resourceId, currentUse, requirement, capacity);
    }

    private int recalculateConsumptionOnAddition(final int[] totalUse, final int resourceId, final int currentTotalUse, final int requirement, final int capacity) {
        final int newTotalUse = requirement + currentTotalUse;
        totalUse[resourceId] = newTotalUse;
        if (currentTotalUse > capacity) {
            // add the increase over the already overreached capacity
            return requirement;
        } else if (newTotalUse > capacity) {
            // the capacity is newly overreached
            return (newTotalUse - capacity);
        } else {
            // the capacity remains idle
            return 0;
        }
    }

    private int recalculateConsumptionOnRemoval(final int[] totalUse, final int resourceId, final int currentTotalUse, final int requirement, final int capacity) {
        final int newTotalUse = currentTotalUse - requirement;
        totalUse[resourceId] = newTotalUse;
        if (newTotalUse > capacity) {
            // remove the decrease over the already overreached capacity
            return requirement;
        } else if (currentTotalUse > capacity) {
            // the capacity is newly idle
            return (currentTotalUse - capacity);
        } else {
            // the capacity remains idle
            return 0;
        }
    }

    public void remove(final TaskAssignment a) {
        this.overused -= this.process(a, false);
    }

}