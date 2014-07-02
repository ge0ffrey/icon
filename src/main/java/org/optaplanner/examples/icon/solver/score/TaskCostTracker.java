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
        final Period onePastEnd = ta.getFinalPeriod().next();
        Period p = ta.getStartPeriod();
        long tempCost = 0;
        while (p != onePastEnd) {
            tempCost += this.forecast.getForPeriod(p).getCost();
            p = p.next();
        }
        final long totalCost = FixedPointArithmetic.multiply(ta.getTask().getPowerConsumption(), tempCost);
        if (isAdding) {
            this.cost += totalCost;
        } else {
            this.cost -= totalCost;
        }
    }

    public void remove(final TaskAssignment ta) {
        this.process(ta, false);
    }

}
