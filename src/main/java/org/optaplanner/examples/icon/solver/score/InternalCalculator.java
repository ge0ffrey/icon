package org.optaplanner.examples.icon.solver.score;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Resource;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

final class InternalCalculator {

    private long idleCosts = 0;
    private long shutdownCosts = 0;
    private final Schedule sched;
    private long startupCosts = 0;
    private long taskCosts = 0;

    public InternalCalculator(final Schedule sched) {
        this.sched = sched;
    }

    private void addIdle(final Machine m, final Period start, final Period end) {
        for (int i = start.getId(); i <= end.getId(); i++) {
            final Period p = Period.get(i);
            this.idleCosts += this.getCost(p, m.getCostWhenIdle());
        }
    }

    private void addShutdown(final Machine m) {
        this.shutdownCosts += m.getCostOnShutdown();
    }

    private void addStartup(final Machine m) {
        this.startupCosts += m.getCostOnStartup();
    }

    private void addTask(final TaskAssignment t, final Period p) {
        this.taskCosts += this.getCost(p, t.getTask().getPowerConsumption());
    }

    private long getCost(final Period p, final long partialCost) {
        final long cost = this.sched.getForecast().getForPeriod(p).getCost();
        return FixedPointArithmetic.multiply(cost, partialCost);
    }

    public long hardScore() {
        long overuse = 0;
        for (final Machine m : this.sched.getMachines()) {
            final Map<Period, Map<Resource, Integer>> consumptions = new TreeMap<Period, Map<Resource, Integer>>();
            // find out how much of the resources we consume per period
            for (final TaskAssignment t : this.sched.getTaskAssignments()) {
                if (t.getExecutor() != m) {
                    continue;
                }
                int startId = t.getStartPeriod().getId();
                int endId = t.getFinalPeriod().getId();
                for (int resourceId = 0; resourceId < this.sched.getResourceCount(); resourceId++) {
                    final Resource r = Resource.get(resourceId);
                    for (int periodId = startId; periodId <= endId; periodId++) {
                        final Period period = Period.get(periodId);
                        if (!consumptions.containsKey(period)) {
                            consumptions.put(period, new HashMap<Resource, Integer>());
                        }
                        final Map<Resource, Integer> consumption = consumptions.get(period);
                        if (consumption.containsKey(r)) {
                            consumption.put(r, consumption.get(r) + t.getTask().getConsumption(r));
                        } else {
                            consumption.put(r, t.getTask().getConsumption(r));
                        }
                    }
                }
            }
            // and now calculate overuse
            for (final Map<Resource, Integer> entry : consumptions.values()) {
                for (final Map.Entry<Resource, Integer> pair : entry.entrySet()) {
                    final int capacity = m.getCapacity(pair.getKey()).getCapacity();
                    final int use = pair.getValue();
                    if (use > capacity) {
                        overuse -= use - capacity;
                    }
                }
            }
        }
        return overuse;
    }

    public long softScore() {
        final Map<Machine, Set<Period>> runningTasks = new HashMap<Machine, Set<Period>>();
        // get costs for running tasks
        for (final TaskAssignment ta : this.sched.getTaskAssignments()) {
            if (!ta.isInitialized()) {
                continue;
            }
            final Machine m = ta.getExecutor();
            if (!runningTasks.containsKey(m)) {
                runningTasks.put(m, new TreeSet<Period>());
            }
            final Set<Period> activePeriods = runningTasks.get(m);
            for (int i = ta.getStartPeriod().getId(); i <= ta.getFinalPeriod().getId(); i++) {
                final Period p = Period.get(i);
                activePeriods.add(p);
                this.addTask(ta, p);
            }
        }
        // get costs for shutdowns and startups
        final int maxPeriodId = 1440 / this.sched.getResolution() - 1;
        for (final Map.Entry<Machine, Set<Period>> entry : runningTasks.entrySet()) {
            final Machine m = entry.getKey();
            final Set<Period> running = entry.getValue();
            // find all gaps where the machine is idle
            final List<Pair<Period, Period>> gaps = new LinkedList<Pair<Period, Period>>();
            boolean isInGap = false;
            SortedSet<Period> gap = new TreeSet<Period>();
            for (int i = 0; i <= maxPeriodId; i++) {
                final Period checked = Period.get(i);
                if (running.contains(checked)) {
                    if (isInGap) {
                        // end gap
                        isInGap = false;
                        if (!gap.contains(Period.get(0)) && !gap.contains(Period.get(maxPeriodId))) {
                            /*
                             * gaps that include 0 and or max aren't gaps. they are pre-first startup and post-last
                             * shutdown
                             */
                            gaps.add(new Pair<Period, Period>(gap.first(), gap.last()));
                        }
                        gap = new TreeSet<Period>();
                    } else {
                        continue;
                    }
                } else {
                    if (!isInGap) {
                        isInGap = true;
                    }
                    gap.add(checked);
                }
            }
            // now go through the gaps and properly account for their costs
            for (final Pair<Period, Period> idle : gaps) {
                final Period start = idle.getFirst();
                final Period end = idle.getSecond();
                boolean willShutdown = false;
                for (final TaskAssignment ta : this.sched.getTaskAssignments()) {
                    if (ta.getExecutor() != m) {
                        continue;
                    } else if (ta.getFinalPeriod().getId() != start.getId() - 1) {
                        continue;
                    }
                    if (ta.getShutdownPossible()) {
                        willShutdown = true;
                        break;
                    }
                }
                if (willShutdown) {
                    // add the costs of starting up and shutting down
                    this.addStartup(m);
                    this.addShutdown(m);
                } else {
                    // add the power costs for keeping the machine alive
                    this.addIdle(m, start, end);
                }
            }
            // each machine gets one startup and shutdown by default
            this.addStartup(m);
            this.addShutdown(m);
            for (final Period p : entry.getValue()) {
                this.idleCosts += this.getCost(p, m.getCostWhenIdle());
            }
        }
        return 0 - this.taskCosts - this.idleCosts - this.startupCosts - this.shutdownCosts;
    }

}