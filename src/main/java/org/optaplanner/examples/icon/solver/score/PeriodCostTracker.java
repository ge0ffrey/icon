package org.optaplanner.examples.icon.solver.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
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

    private long latestValuation;

    private final Machine machine;

    private final Schedule schedule;

    public PeriodCostTracker(final Schedule schedule, final Machine m) {
        this.schedule = schedule;
        this.machine = m;
        this.latestValuation = this.valuateIdleTime();
    }

    public long add(final TaskAssignment ta) {
        final long valuationBefore = this.latestValuation;
        // if we're adding the first task, the machine becomes active. add one default startup/shutdown cost
        long costChange = this.activeTasks.isEmpty() ? ta.getExecutor().getCostOfRespin() : 0;
        for (int i = ta.getStartPeriod().getId(); i <= ta.getFinalPeriod().getId(); i++) {
            final Period p = Period.get(i);
            if (!this.activeTasks.containsKey(p)) {
                this.activeTasks.put(p, new HashSet<TaskAssignment>());
                // adding a new period when the machine is definitely running
                costChange += FixedPointArithmetic.multiply(this.machine.getCostWhenIdle(), this.schedule.getForecast().getForPeriod(p).getCost());
            }
            this.activeTasks.get(p).add(ta);
        }
        final long valuationAfter = this.valuateIdleTime();
        final long differenceInValuation = valuationAfter - valuationBefore;
        return costChange + differenceInValuation;
    }

    public long getCost() {
        return this.cost;
    }

    private long getGapCost(final Period start, final Period end) {
        long totalCost = 0;
        final long idleCost = this.getIdleCost(start, end);
        // properly account for idle costs
        for (final TaskAssignment ta : this.schedule.getTaskAssignments()) {
            if (ta.getExecutor() != this.machine) {
                // irrelevant to this tracker
                continue;
            } else if (ta.getFinalPeriod().getId() != start.getId() - 1) {
                continue;
            }
            if (idleCost > this.machine.getCostOfRespin()) {
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
        for (int i = start.getId(); i <= end.getId(); i++) {
            final Period p = Period.get(i);
            idleCost += FixedPointArithmetic.multiply(this.schedule.getForecast().getForPeriod(p).getCost(), this.machine.getCostWhenIdle());
        }
        return idleCost;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public long remove(final TaskAssignment ta) {
        long costChange = 0;
        final long valuationBefore = this.latestValuation;
        for (int i = ta.getStartPeriod().getId(); i <= ta.getFinalPeriod().getId(); i++) {
            final Period p = Period.get(i);
            final Set<TaskAssignment> runningTasks = this.activeTasks.get(p);
            runningTasks.remove(ta);
            if (runningTasks.isEmpty()) {
                this.activeTasks.remove(p);
                // removing a period when the machine is definitely running
                costChange += FixedPointArithmetic.multiply(this.machine.getCostWhenIdle(), this.schedule.getForecast().getForPeriod(p).getCost());
            }
        }
        if (this.activeTasks.size() == 0) {
            // the machine is never started or stopped; change the constraints
            costChange += ta.getExecutor().getCostOfRespin();
        }
        final long valuationAfter = this.valuateIdleTime();
        final long differenceInValuation = valuationAfter - valuationBefore;
        return costChange - differenceInValuation;
    }

    // FIXME this is not very incremental
    private long valuateIdleTime() {
        long cost = 0;
        // get costs for shutdowns and startups
        final int maxPeriodId = 1440 / this.schedule.getResolution() - 1;
        final Set<Period> running = this.activeTasks.keySet();
        new LinkedList<Pair<Period, Period>>();
        boolean isInGap = false;
        Period firstInGap = null;
        for (int i = 0; i <= maxPeriodId; i++) {
            final Period current = Period.get(i);
            if (running.contains(current)) {
                if (isInGap) {
                    // end gap
                    isInGap = false;
                    final Period lastInGap = Period.get(current.getId() - 1);
                    if (firstInGap == Period.get(0) || lastInGap == Period.get(maxPeriodId)) {
                        /*
                         * gaps that include 0 or max aren't gaps. they are pre-first startup and post-last
                         * shutdown
                         */
                        continue;
                    }
                    cost += this.getGapCost(firstInGap, lastInGap);
                } else {
                    continue;
                }
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
