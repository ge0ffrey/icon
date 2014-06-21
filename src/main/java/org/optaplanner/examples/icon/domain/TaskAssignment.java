package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.Collection;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TaskAssignment {

    private Machine executor;

    private Period finalPeriod;

    private Forecast forecast;

    private boolean mayShutdownOnCompletion = false;

    private BigDecimal powerCost;

    private Period startPeriod;
    private Task task;

    protected TaskAssignment() {
        // FIXME planner cloning prevents immutability
    }

    public TaskAssignment(final Task task, final Forecast forecast) {
        this.task = task;
        this.forecast = forecast;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TaskAssignment)) {
            return false;
        }
        final TaskAssignment other = (TaskAssignment) obj;
        if (this.task == null) {
            if (other.task != null) {
                return false;
            }
        } else if (!this.task.equals(other.task)) {
            return false;
        }
        return true;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleExecutorRange"})
    public Machine getExecutor() {
        return this.executor;
    }

    // FIXME changes with start period; should be shadow?
    public Period getFinalPeriod() {
        return this.finalPeriod;
    }

    @ValueRangeProvider(id = "possibleExecutorRange")
    public Collection<Machine> getPossibleExecutors() {
        return this.task.getAvailableMachines();
    }

    // FIXME changes with start period; should be shadow?
    public BigDecimal getPowerCost() {
        return this.powerCost;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleShutdownRange"})
    public boolean getShutdownPossible() {
        return this.mayShutdownOnCompletion;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleStartPeriodRange"})
    public Period getStartPeriod() {
        return this.startPeriod;
    }

    @ValueRangeProvider(id = "possibleStartPeriodRange")
    public ValueRange<Period> getStartPeriodValueRange() {
        return this.task.getAvailableStartPeriodRange();
    }

    public Task getTask() {
        return this.task;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.task == null) ? 0 : this.task.hashCode());
        return result;
    }

    // FIXME changes with start period; should be shadow?
    public boolean isInitialized() {
        return this.executor != null && this.startPeriod != null;
    }

    public void setExecutor(final Machine executor) {
        this.executor = executor;
    }

    public void setShutdownPossible(final boolean shutdownPossible) {
        this.mayShutdownOnCompletion = shutdownPossible;
    }

    public void setStartPeriod(final Period startPeriod) {
        if (this.startPeriod == startPeriod) {
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
