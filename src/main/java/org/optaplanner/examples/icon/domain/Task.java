package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.valuerange.buildin.primint.IntValueRange;

@PlanningEntity
public class Task {

    // variables
    private int actualStartDate;
    // constants
    private int dueBy;
    private int duration;
    private int earliestStart;
    private Machine executor;
    private int id;

    private BigDecimal powerConsumption;
    private int[] resourceConsumption;

    protected Task() {
        // FIXME planner cloning prevents immutability
    }

    public Task(final int id, final int duration, final int earliestStart, final int dueBy, final BigDecimal powerUse, final List<Integer> resourceConsumption) {
        this.id = id;
        this.duration = duration;
        this.earliestStart = earliestStart;
        this.dueBy = dueBy;
        this.powerConsumption = powerUse;
        this.resourceConsumption = new int[resourceConsumption.size()];
        int i = 0;
        for (final Integer consumption : resourceConsumption) {
            this.resourceConsumption[i] = consumption;
            i++;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        final Task other = (Task) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleStartDateRange"})
    public int getActualStartDate() {
        return this.actualStartDate;
    }

    public int getDueBy() {
        return this.dueBy;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getEarliestStart() {
        return this.earliestStart;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleExecutorRange"})
    public Machine getExecutor() {
        return this.executor;
    }

    public int getId() {
        return this.id;
    }

    public BigDecimal getPowerConsumption() {
        return this.powerConsumption;
    }

    public int getResourceConsumption(final int resourceId) {
        return this.resourceConsumption[resourceId];
    }

    @ValueRangeProvider(id = "possibleStartDateRange")
    public IntValueRange getStartDateValueRange() {
        return new IntValueRange(this.getEarliestStart(), this.getDueBy() - this.getDuration());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

    public void setActualStartDate(final int actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public void setExecutor(final Machine executor) {
        this.executor = executor;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Task [id=").append(this.id).append(", duration=").append(this.duration).append(", earliestStart=").append(this.earliestStart).append(", dueBy=").append(this.dueBy).append(", ");
        if (this.powerConsumption != null) {
            builder.append("powerConsumption=").append(this.powerConsumption).append(", ");
        }
        if (this.resourceConsumption != null) {
            builder.append("resourceConsumption=").append(Arrays.toString(this.resourceConsumption));
        }
        builder.append("]");
        return builder.toString();
    }

}
