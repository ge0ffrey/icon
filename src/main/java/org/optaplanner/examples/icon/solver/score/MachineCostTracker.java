package org.optaplanner.examples.icon.solver.score;

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
    private final PeriodCostTracker[] subtrackers;

    public MachineCostTracker(final Schedule schedule) {
        this.schedule = schedule;
        this.subtrackers = new PeriodCostTracker[schedule.getMachines().size()];
    }

    public void add(final TaskAssignment ta) {
        final Machine m = ta.getExecutor();
        final int id = m.getId();
        PeriodCostTracker t = this.subtrackers[id];
        if (t == null) {
            t = this.subtrackers[id] = new PeriodCostTracker(this.schedule, m);
        }
        this.cost += t.add(ta);
    }

    public long getCost() {
        return this.cost;
    }

    public void modify(final TaskAssignment ta, final Period previousStartPeriod, final Period previousFinalPeriod) {
        final Machine m = ta.getExecutor();
        final int id = m.getId();
        this.cost += this.subtrackers[id].modify(ta, previousStartPeriod, previousFinalPeriod);
    }

    public void remove(final TaskAssignment ta) {
        final Machine m = ta.getExecutor();
        final int id = m.getId();
        this.cost -= this.subtrackers[id].remove(ta);
    }

}
