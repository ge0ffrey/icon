package org.optaplanner.examples.icon.solver.score;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

import org.optaplanner.examples.icon.domain.Forecast;
import org.optaplanner.examples.icon.domain.Period;
import org.optaplanner.examples.icon.domain.Schedule;
import org.optaplanner.examples.icon.domain.TaskAssignment;
import org.optaplanner.examples.icon.util.FixedPointArithmetic;

public class TaskCostTracker {

    private long cost = 0;

    private final Int2LongMap costCache = new Int2LongOpenHashMap();
    private final Forecast forecast;

    public TaskCostTracker(final Schedule schedule) {
        this.forecast = schedule.getForecast();
    }

    public void add(final TaskAssignment ta) {
        /*
         * calculate new cost for the task; the caching is beneficial, as some of the tasks have far too long durations,
         * which leads to a lot of iteration. we can prevent this duplicate effort during removal by caching here.
         */
        final long taskCost = this.calculateCost(ta);
        this.costCache.put(ta.getTask().getId(), taskCost);
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

    public void remove(final TaskAssignment ta) {
        /*
         * we do not remove the cost from cache; this is technically stale data now, but the remove operation is not
         * worth the cost in execution time
         */
        this.cost -= this.costCache.get(ta.getTask().getId());
    }

}
