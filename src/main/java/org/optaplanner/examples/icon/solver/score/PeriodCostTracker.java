package org.optaplanner.examples.icon.solver.score;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

/**
 * Calculates costs for idle time and or startup+shutdown.
 */
public class PeriodCostTracker {

    private final Map<Period, Set<TaskAssignment>> activeTasks;
    private final long cost = 0;

    private final int estimatedTasksPerMachine;

    private final Period firstPeriod = Period.get(0);

    private final Forecast forecast;

    private final Period lastPeriod;

    private long latestValuation;
    private final Machine machine;

    public PeriodCostTracker(final Schedule schedule, final Machine m) {
        this.forecast = schedule.getForecast();
        this.estimatedTasksPerMachine = 2 * (1 + (schedule.getTaskAssignments().size() / schedule.getMachines().size()));
        this.machine = m;
        this.lastPeriod = Period.get(1440 / schedule.getResolution() - 1);
        this.activeTasks = new LinkedHashMap<Period, Set<TaskAssignment>>(this.lastPeriod.getId() + 1);
        this.latestValuation = this.valuateIdleTime();
    }

    public long add(final TaskAssignment ta) {
        // if we're adding the first task, the machine becomes active. add one default startup/shutdown cost
        final long costChange = this.activeTasks.isEmpty() ? ta.getExecutor().getCostOfRespin() : 0;
        return costChange + this.addPeriod(ta, ta.getStartPeriod(), ta.getFinalPeriod());
    }

    private long addPeriod(final TaskAssignment ta, final Period start, final Period last) {
        boolean idleInformationChanged = false;
        final Period oneAfterLast = last.next();
        long costChange = 0;
        long tempCost = 0;
        Period current = start;
        while (current != oneAfterLast) {
            Set<TaskAssignment> tasks = this.activeTasks.get(current);
            if (tasks == null) {
                // period is no longer idle
                tasks = new LinkedHashSet<TaskAssignment>(this.estimatedTasksPerMachine);
                this.activeTasks.put(current, tasks);
                tempCost += this.forecast.getForPeriod(current).getCost();
                idleInformationChanged = true;
            }
            tasks.add(ta);
            current = current.next();
        }
        costChange += FixedPointArithmetic.multiply(this.machine.getCostWhenIdle(), tempCost);
        if (idleInformationChanged) {
            costChange += this.valuateIdleTime();
        }
        return costChange;
    }

    public long getCost() {
        return this.cost;
    }

    private long getGapCost(final Period start, final Period end) {
        final long idleCost = this.getIdleCost(start, end);
        final boolean shutdownPossible = idleCost > this.machine.getCostOfRespin();
        for (final TaskAssignment ta : this.activeTasks.get(start.previous())) {
            ta.setShutdownPossible(shutdownPossible);
        }
        return shutdownPossible ? this.machine.getCostOfRespin() : idleCost;
    }

    private long getIdleCost(final Period start, final Period end) {
        long idleCost = 0;
        Period current = start;
        final Period oneAfterLast = end.next();
        while (current != oneAfterLast) {
            idleCost += this.forecast.getForPeriod(current).getCost();
            current = current.next();
        }
        return FixedPointArithmetic.multiply(idleCost, this.machine.getCostWhenIdle());
    }

    public long modify(final TaskAssignment ta, final Period previousStartPeriod, final Period previousFinalPeriod) {
        final int previousStart = previousStartPeriod.getId();
        final int previousFinal = previousFinalPeriod.getId();
        final Period currentStartPeriod = ta.getStartPeriod();
        final Period currentFinalPeriod = ta.getFinalPeriod();
        final int currentStart = currentStartPeriod.getId();
        final int currentFinal = currentFinalPeriod.getId();

        long totalChange = 0;
        final int periodsToRecalculate = Math.abs(previousStart - currentStart);
        final boolean differentialMakesSense = periodsToRecalculate < ta.getTask().getDuration();
        if (previousStart > currentStart) {
            // task moves to the left
            final boolean hasOverlap = currentFinal >= previousStart;
            if (!hasOverlap || !differentialMakesSense) {
                // optimization makes no sense; just make a direct remove/add
                ta.setStartPeriod(previousStartPeriod);
                totalChange -= this.remove(ta);
                ta.setStartPeriod(currentStartPeriod);
                totalChange += this.add(ta);
            } else {
                // there is overlap
                totalChange += this.addPeriod(ta, currentStartPeriod, previousStartPeriod.previous());
                totalChange -= this.removePeriod(ta, currentFinalPeriod.next(), previousFinalPeriod);
            }
        } else if (currentStart > previousStart) {
            // task moves to the right
            final boolean hasOverlap = previousFinal >= currentStart;
            if (!hasOverlap || !differentialMakesSense) {
                // optimization makes no sense; just make a direct remove/add
                ta.setStartPeriod(previousStartPeriod);
                totalChange -= this.remove(ta);
                ta.setStartPeriod(currentStartPeriod);
                totalChange += this.add(ta);
            } else {
                totalChange -= this.removePeriod(ta, previousStartPeriod, currentStartPeriod.previous());
                totalChange += this.addPeriod(ta, previousFinalPeriod.next(), currentFinalPeriod);
            }
        }
        return totalChange;
    }

    public long remove(final TaskAssignment ta) {
        final Period current = ta.getStartPeriod();
        long costChange = this.removePeriod(ta, current, ta.getFinalPeriod());
        if (this.activeTasks.size() == 0) {
            // the machine is never started or stopped; change the constraints
            costChange += ta.getExecutor().getCostOfRespin();
        }
        return costChange;
    }

    private long removePeriod(final TaskAssignment ta, final Period first, final Period last) {
        long costChange = 0;
        long tempCost = 0;
        boolean idleInformationChanged = false;
        final Period oneAfterLast = last.next();
        Period current = first;
        while (current != oneAfterLast) {
            final Set<TaskAssignment> runningTasks = this.activeTasks.get(current);
            if (runningTasks.size() == 1) {
                // we know that this is the last task in the period; period will go idle
                this.activeTasks.remove(current);
                tempCost += this.forecast.getForPeriod(current).getCost();
                idleInformationChanged = true;
            } else {
                runningTasks.remove(ta);
            }
            current = current.next();
        }
        costChange += FixedPointArithmetic.multiply(this.machine.getCostWhenIdle(), tempCost);
        if (idleInformationChanged) {
            costChange -= this.valuateIdleTime();
        }
        return costChange;
    }

    // FIXME this is not very incremental
    private long valuateIdleTime() {
        long cost = 0;
        // get costs for shutdowns and startups
        final Set<Period> running = this.activeTasks.keySet();
        boolean isInGap = false;
        Period firstInGap = null;
        Period p = this.firstPeriod;
        final Period oneAfterLast = this.lastPeriod.next();
        while (p != oneAfterLast) {
            final Period current = p;
            p = p.next(); // do this now, as there is a lot of "continue" later
            if (running.contains(current)) {
                if (!isInGap) {
                    continue;
                }
                // end gap
                isInGap = false;
                final Period lastInGap = current.previous();
                if (firstInGap == this.firstPeriod || lastInGap == this.lastPeriod) {
                    /*
                     * gaps that include 0 or max aren't gaps. they are pre-first startup and post-last
                     * shutdown
                     */
                    continue;
                }
                cost += this.getGapCost(firstInGap, lastInGap);
            } else {
                if (!isInGap) {
                    isInGap = true;
                    firstInGap = current;
                }
            }
        }
        final long previousValuation = this.latestValuation;
        this.latestValuation = cost;
        return cost - previousValuation;
    }

}
