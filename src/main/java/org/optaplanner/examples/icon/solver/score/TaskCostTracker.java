package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

public class TaskCostTracker {

    private long cost = 0;

    private final Schedule schedule;

    public TaskCostTracker(final Schedule schedule) {
        this.schedule = schedule;
    }

    public void add(final TaskAssignment ta) {
        this.process(ta, true);
    }

    public long getCost() {
        return this.cost;
    }

    private void process(final TaskAssignment ta, final boolean isAdding) {
        final Period start = ta.getStartPeriod();
        final Period end = ta.getFinalPeriod();
        final long powerConsumption = ta.getTask().getPowerConsumption();
        for (int i = start.getId(); i <= end.getId(); i++) {
            final long periodCost = this.schedule.getForecast().getForPeriod(Period.get(i)).getCost();
            final long totalCost = FixedPointArithmetic.multiply(powerConsumption, periodCost);
            if (isAdding) {
                this.cost += totalCost;
            } else {
                this.cost -= totalCost;
            }
        }
    }

    public void remove(final TaskAssignment ta) {
        this.process(ta, false);
    }

}
