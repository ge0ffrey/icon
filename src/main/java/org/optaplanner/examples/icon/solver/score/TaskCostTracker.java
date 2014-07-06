package org.optaplanner.examples.icon.solver.score;

import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

public class TaskCostTracker {

    private long cost = 0;

    private final long[] costCache;
    private final Forecast forecast;

    public TaskCostTracker(final Schedule schedule) {
        this.forecast = schedule.getForecast();
        this.costCache = new long[schedule.getTaskAssignments().size()];
    }

    public void add(final TaskAssignment ta) {
        /*
         * calculate new cost for the task; the caching is beneficial, as some of the tasks have far too long durations,
         * which leads to a lot of iteration. we can prevent this duplicate effort during removal by caching here.
         */
        final long taskCost = this.calculateCost(ta);
        final int taskId = ta.getTask().getId();
        this.costCache[taskId] = taskCost;
        // incur the penalty
        this.cost += taskCost;
    }

    private long calculateCost(final TaskAssignment ta) {
        final Period onePastEnd = ta.getFinalPeriod().next();
        Period p = ta.getStartPeriod();
        long tempCost = 0;
        while (p != onePastEnd) {
            tempCost += this.forecast.getForPeriod(p).getCost();
            p = p.next();
        }
        return FixedPointArithmetic.multiply(ta.getTask().getPowerConsumption(), tempCost);
    }

    public long getCost() {
        return this.cost;
    }

    public void modify(final TaskAssignment ta) {
        /*
         * due to limitations in FixedPointArithmetic (see that class' comments), this stupid remove+add is faster than
         * running this class refactored so that the multiplications happen on every cost retrieval.
         */
        this.remove(ta);
        this.add(ta);
    }

    public void remove(final TaskAssignment ta) {
        this.cost -= this.costCache[ta.getTask().getId()];
    }

}
