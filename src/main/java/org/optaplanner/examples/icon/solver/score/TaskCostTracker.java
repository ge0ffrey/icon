package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

public class TaskCostTracker {

    private long cost = 0;

    private final Forecast forecast;

    public TaskCostTracker(final Schedule schedule) {
        this.forecast = schedule.getForecast();
    }

    public void add(final TaskAssignment ta) {
        this.process(ta, true);
    }

    public long getCost() {
        return this.cost;
    }

    private void process(final TaskAssignment ta, final boolean isAdding) {
        final long powerConsumption = ta.getTask().getPowerConsumption();
        final Period onePastEnd = ta.getFinalPeriod().next();
        Period p = ta.getStartPeriod();
        while (p != onePastEnd) {
            final long periodCost = this.forecast.getForPeriod(p).getCost();
            final long totalCost = FixedPointArithmetic.multiply(powerConsumption, periodCost);
            if (isAdding) {
                this.cost += totalCost;
            } else {
                this.cost -= totalCost;
            }
            p = p.next();
        }
    }

    public void remove(final TaskAssignment ta) {
        this.process(ta, false);
    }

}
