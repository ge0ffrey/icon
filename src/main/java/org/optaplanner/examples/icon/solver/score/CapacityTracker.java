package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.examples.icon.domain.Machine;
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
        this.process(a, true);
    }

    private int[] getConsumptionInTime(final Machine m, final int time) {
        final int[][] consumption = this.getConsumptionsForMachine(m);
        final int[] totalUse = consumption[time];
        if (totalUse == null) {
            return consumption[time] = new int[this.resourceCount + 1];
        } else {
            return totalUse;
        }
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

    private void modifyConsumption(final int resourceId, final int requirement, final int resourceCapacity, final int[] consumptionInTime, final boolean isAdding) {
        consumptionInTime[resourceId] = this.recalculateConsumption(consumptionInTime[resourceId], requirement, resourceCapacity, isAdding);
    }

    private void process(final TaskAssignment a, final boolean isAdding) {
        final int startDate = a.getStartPeriod().getId();
        final int dueDate = a.getFinalPeriod().getId();
        final Machine m = a.getExecutor();
        for (final ResourceRequirement rr : a.getTask().getConsumptions()) {
            this.processRequirement(startDate, dueDate, m, rr, isAdding);
        }
    }

    private void processRequirement(final int startDate, final int dueDate, final Machine m, final ResourceRequirement rr, final boolean isAdding) {
        final Resource resource = rr.getResource();
        final int requirement = rr.getRequirement();
        final int resourceId = resource.getId();
        final int capacity = m.getCapacity(rr.getResource()).getCapacity();
        for (int time = startDate; time <= dueDate; time++) {
            this.modifyConsumption(resourceId, requirement, capacity, this.getConsumptionInTime(m, time), isAdding);
        }
    }

    private int recalculateConsumption(final int consumption, final int newRequirement, final int resourceCapacity, final boolean isAdding) {
        return isAdding ?
                this.recalculateConsumptionOnAddition(consumption, newRequirement, resourceCapacity) :
                this.recalculateConsumptionOnRemoval(consumption, newRequirement, resourceCapacity);
    }

    private int recalculateConsumptionOnAddition(final int currentTotalUse, final int requirement, final int capacity) {
        final int newTotalUse = requirement + currentTotalUse;
        if (currentTotalUse > capacity) {
            // add the increase over the already overreached capacity
            this.overused += requirement;
        } else if (newTotalUse > capacity) {
            // the capacity is newly overreached
            this.overused += (newTotalUse - capacity);
        } else {
            // the capacity remains idle
        }
        return newTotalUse;
    }

    private int recalculateConsumptionOnRemoval(final int currentTotalUse, final int requirement, final int capacity) {
        final int newTotalUse = currentTotalUse - requirement;
        if (newTotalUse > capacity) {
            // remove the decrease over the already overreached capacity
            this.overused -= requirement;
        } else if (currentTotalUse > capacity) {
            // the capacity is newly idle
            this.overused -= (currentTotalUse - capacity);
        } else {
            // the capacity remains idle
        }
        return newTotalUse;
    }

    public void remove(final TaskAssignment a) {
        this.process(a, false);
    }

}