package org.optaplanner.examples.icon.solver.score;

import java.util.HashMap;
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

    private final Map<Period, Set<TaskAssignment>> activeTasks = new HashMap<Period, Set<TaskAssignment>>();
    private final long cost = 0;

    private final int estimatedTasksPerMachine;

    private final Period firstPeriod = Period.get(0);

    private final Forecast forecast;

    private final Set<TaskAssignment> knownTasks;
    private final Period lastPeriod;

    private long latestValuation;
    private final Machine machine;

    public PeriodCostTracker(final Schedule schedule, final Machine m) {
        this.forecast = schedule.getForecast();
        this.estimatedTasksPerMachine = 2 * (1 + (schedule.getTaskAssignments().size() / schedule.getMachines().size()));
        this.machine = m;
        this.lastPeriod = Period.get(1440 / schedule.getResolution() - 1);
        this.latestValuation = this.valuateIdleTime();
        this.knownTasks = new LinkedHashSet<TaskAssignment>(this.estimatedTasksPerMachine);
    }

    public long add(final TaskAssignment ta) {
        this.knownTasks.add(ta);
        // if we're adding the first task, the machine becomes active. add one default startup/shutdown cost
        long costChange = this.activeTasks.isEmpty() ? ta.getExecutor().getCostOfRespin() : 0;
        Period current = ta.getStartPeriod();
        final Period oneAfterLast = ta.getFinalPeriod().next();
        long tempCost = 0;
        boolean idleInformationChanged = false;
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
            final long valuationBefore = this.latestValuation;
            final long valuationAfter = this.valuateIdleTime();
            final long differenceInValuation = valuationAfter - valuationBefore;
            costChange += differenceInValuation;
        }
        return costChange;
    }

    public long getCost() {
        return this.cost;
    }

    private long getGapCost(final Period start, final Period end) {
        long totalCost = 0;
        final long idleCost = this.getIdleCost(start, end);
        // properly account for idle costs
        for (final TaskAssignment ta : this.knownTasks) {
            if (ta.getFinalPeriod() != start.previous()) {
                continue;
            } else if (idleCost > this.machine.getCostOfRespin()) {
                ta.setShutdownPossible(true);
                totalCost += this.machine.getCostOfRespin();
            } else {
                ta.setShutdownPossible(false);
                totalCost += idleCost;
            }
        }
        return totalCost;
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

    public long remove(final TaskAssignment ta) {
        long costChange = 0;
        Period current = ta.getStartPeriod();
        final Period oneAfterLast = ta.getFinalPeriod().next();
        long tempCost = 0;
        boolean idleInformationChanged = false;
        while (current != oneAfterLast) {
            final Set<TaskAssignment> runningTasks = this.activeTasks.get(current);
            runningTasks.remove(ta);
            if (runningTasks.isEmpty()) {
                // period goes idle
                this.activeTasks.remove(current);
                tempCost += this.forecast.getForPeriod(current).getCost();
                idleInformationChanged = true;
            }
            current = current.next();
        }
        costChange += FixedPointArithmetic.multiply(this.machine.getCostWhenIdle(), tempCost);
        if (idleInformationChanged) {
            final long valuationBefore = this.latestValuation;
            final long valuationAfter = this.valuateIdleTime();
            final long differenceInValuation = valuationAfter - valuationBefore;
            costChange -= differenceInValuation;
        }
        if (this.activeTasks.size() == 0) {
            // the machine is never started or stopped; change the constraints
            costChange += ta.getExecutor().getCostOfRespin();
        }
        this.knownTasks.remove(ta);
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
        this.latestValuation = cost;
        return cost;
    }

}
