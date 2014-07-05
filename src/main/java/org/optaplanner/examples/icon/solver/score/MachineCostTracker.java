package org.optaplanner.examples.icon.solver.score;

import java.util.LinkedHashMap;
import java.util.Map;

import org.optaplanner.examples.icon.domain.Machine;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;

/**
 * Calculates costs for idle time and or startup+shutdown.
 */
public class MachineCostTracker {

    private long cost = 0;

    private final Schedule schedule;
    private final Map<Machine, PeriodCostTracker> subtrackers = new LinkedHashMap<Machine, PeriodCostTracker>();

    public MachineCostTracker(final Schedule schedule) {
        this.schedule = schedule;
    }

    public void add(final TaskAssignment ta) {
        final Machine m = ta.getExecutor();
        PeriodCostTracker t = this.subtrackers.get(m);
        if (t == null) {
            t = new PeriodCostTracker(this.schedule, m);
            this.subtrackers.put(m, t);
        }
        this.cost += t.add(ta);
    }

    public long getCost() {
        return this.cost;
    }

    public void modify(final TaskAssignment ta, final Period previousStartPeriod, final Period previousFinalPeriod) {
        final Machine m = ta.getExecutor();
        this.cost += this.subtrackers.get(m).modify(ta, previousStartPeriod, previousFinalPeriod);
    }

    public void remove(final TaskAssignment ta) {
        final Machine m = ta.getExecutor();
        this.cost -= this.subtrackers.get(m).remove(ta);
    }

}
