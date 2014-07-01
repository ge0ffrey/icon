package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.Collection;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity(difficultyComparatorClass = TaskAssignmentDifficultyComparator.class)
public class TaskAssignment {

    private Task task;
    private Forecast forecast;

    private Machine executor;
    private Period startPeriod;

    private Period finalPeriod;
    private BigDecimal powerCost;
    private boolean mayShutdownOnCompletion = false;

    protected TaskAssignment() {
        // FIXME planner cloning prevents immutability
    }

    public TaskAssignment(final Task task, final Forecast forecast) {
        this.task = task;
        this.forecast = forecast;
    }

    public Task getTask() {
        return this.task;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleExecutorRange"})
    public Machine getExecutor() {
        return this.executor;
    }

    public void setExecutor(final Machine executor) {
        this.executor = executor;
    }

    @ValueRangeProvider(id = "possibleExecutorRange")
    public Collection<Machine> getPossibleExecutors() {
        return this.task.getAvailableMachines();
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleStartPeriodRange"})
    public Period getStartPeriod() {
        return this.startPeriod;
    }

    public void setStartPeriod(final Period startPeriod) {
        if (startPeriod == null) {
            this.finalPeriod = null;
            this.powerCost = BigDecimal.ZERO;
        } else if (this.startPeriod == startPeriod) {
            // no change
            return;
        } else if (!this.getStartPeriodValueRange().contains(startPeriod)) {
            // defensive programming
            throw new IllegalArgumentException("Cannot set start period to: " + startPeriod);
        }
        this.startPeriod = startPeriod;
        // calculate and cache periods that are occupied by this new task assignment
        if (startPeriod == null) {
            this.finalPeriod = null;
            return;
        }
        final int startPeriodId = this.getStartPeriod().getId();
        final int finalPeriodId = startPeriodId + this.task.getDuration() - 1;
        this.finalPeriod = Period.get(finalPeriodId);
        // calculate power cost
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = startPeriodId; i <= finalPeriodId; i++) {
            final BigDecimal costPerPeriod = this.forecast.getForPeriod(Period.get(i)).getCost();
            cost = cost.add(costPerPeriod);
        }
        this.powerCost = cost.multiply(this.task.getPowerConsumption());
    }

    @ValueRangeProvider(id = "possibleStartPeriodRange")
    public ValueRange<Period> getStartPeriodValueRange() {
        return this.task.getAvailableStartPeriodRange();
    }

    // FIXME changes with start period; should be shadow?
    public Period getFinalPeriod() {
        return this.finalPeriod;
    }

    // FIXME changes with start period; should be shadow?
    public BigDecimal getPowerCost() {
        return this.powerCost;
    }

    // FIXME changes with start period; should be shadow?
    public boolean isInitialized() {
        return this.executor != null && this.startPeriod != null;
    }

    public boolean getShutdownPossible() {
        return this.mayShutdownOnCompletion;
    }


    public void setShutdownPossible(final boolean shutdownPossible) {
        this.mayShutdownOnCompletion = shutdownPossible;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TaskAssignment [");
        if (this.executor != null) {
            builder.append("executor=").append(this.executor).append(", ");
        }
        if (this.finalPeriod != null) {
            builder.append("finalPeriod=").append(this.finalPeriod).append(", ");
        }
        builder.append("mayShutdownOnCompletion=").append(this.mayShutdownOnCompletion).append(", ");
        if (this.startPeriod != null) {
            builder.append("startPeriod=").append(this.startPeriod).append(", ");
        }
        if (this.task != null) {
            builder.append("task=").append(this.task);
        }
        builder.append("]");
        return builder.toString();
    }

}
