package org.optaplanner.examples.icon.solver.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

public class IconScoreCalculator implements EasyScoreCalculator<Schedule> {

    private final class InternalCalculator {

        private final BigDecimal costMultiplier;
        private BigDecimal idleCosts = BigDecimal.ZERO;

        private int idleCounts = 0;
        private BigDecimal shutdownCosts = BigDecimal.ZERO;
        private int shutdownCounts = 0;
        private final Schedule sched;
        private BigDecimal startupCosts = BigDecimal.ZERO;
        private int startupCounts = 0;
        private BigDecimal taskCosts = BigDecimal.ZERO;

        public InternalCalculator(final Schedule sched) {
            this.sched = sched;
            this.costMultiplier = BigDecimal.valueOf(sched.getResolution()).divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP);
        }

        private void addIdle(final Machine m, final Period start, final Period end) {
            for (int i = start.getId(); i <= end.getId(); i++) {
                final Period p = Period.get(i);
                this.idleCosts = this.idleCosts.add(this.getCost(p, m.getCostWhenIdle()));
            }
            this.idleCounts++;
        }

        private void addShutdown(final Machine m) {
            this.shutdownCosts = this.shutdownCosts.add(m.getCostOnShutdown());
            this.shutdownCounts++;
        }

        private void addStartup(final Machine m) {
            this.startupCosts = this.startupCosts.add(m.getCostOnStartup());
            this.startupCounts++;
        }

        private void addTask(final TaskAssignment t, final Period p) {
            this.taskCosts = this.taskCosts.add(this.getCost(p, t.getTask().getPowerConsumption()));
        }

        public BigDecimal calculate() {
            final Map<Machine, Set<Period>> runningTasks = new HashMap<Machine, Set<Period>>();
            // get costs for running tasks
            for (final TaskAssignment ta : this.sched.getTaskAssignments()) {
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
                        willShutdown = ta.getShutdownPossible() || willShutdown;
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
            }
            // get costs for machine idle
            for (final Map.Entry<Machine, Set<Period>> entry : runningTasks.entrySet()) {
                final Machine m = entry.getKey();
                // each machine gets one startup and shutdown by default
                this.addStartup(m);
                this.addShutdown(m);
                for (final Period p : entry.getValue()) {
                    this.idleCosts = this.idleCosts.add(this.getCost(p, m.getCostWhenIdle()));
                }
            }
            return this.taskCosts.add(this.idleCosts).add(this.startupCosts).add(this.shutdownCosts).negate();
        }

        private BigDecimal getCost(final Period p, final BigDecimal partialCost) {
            BigDecimal cost = this.sched.getForecast().getForPeriod(p).getCost();
            cost = cost.multiply(partialCost);
            return cost.multiply(this.costMultiplier);
        }

    }

    @Override
    public HardSoftBigDecimalScore calculateScore(final Schedule sched) {
        return HardSoftBigDecimalScore.valueOf(BigDecimal.ZERO, new InternalCalculator(sched).calculate());
    }

}
