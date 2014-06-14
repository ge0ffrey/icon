package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.math.BigDecimal;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.icon.util.PeriodValueRange;

@PlanningEntity
public class Task {

    // constants
    private Period dueBy;
    private int duration;
    private Period earliestStart;
    private Machine executor;
    private Period finalPeriod;

    private int id;
    private boolean mayShutdownOnCompletion;

    private BigDecimal powerConsumption;

    private final Object2IntMap<Resource> resourceConsumption = new Object2IntOpenHashMap<Resource>();

    // variables
    private Period startPeriod;

    protected Task() {
        // FIXME planner cloning prevents immutability
    }

    public Task(final int id, final int duration, final int earliestStart, final int dueBy, final BigDecimal powerUse, final List<Integer> resourceConsumption) {
        this.id = id;
        this.duration = duration;
        this.earliestStart = Period.get(earliestStart);
        this.dueBy = Period.get(dueBy);
        this.powerConsumption = powerUse;
        for (int i = 0; i < resourceConsumption.size(); i++) {
            this.resourceConsumption.put(Resource.get(i), resourceConsumption.get(i));
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

    public Period getDueBy() {
        return this.dueBy;
    }

    public int getDuration() {
        return this.duration;
    }

    public Period getEarliestStart() {
        return this.earliestStart;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleExecutorRange"})
    public Machine getExecutor() {
        return this.executor;
    }

    // FIXME changes with start period; should be shadow?
    public Period getFinalPeriod() {
        return this.finalPeriod;
    }

    public int getId() {
        return this.id;
    }

    public BigDecimal getPowerConsumption() {
        return this.powerConsumption;
    }

    public int getResourceConsumption(final Resource resource) {
        return this.resourceConsumption.getInt(resource);
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
        return new PeriodValueRange(this.getEarliestStart().getId(), this.getDueBy().getId() - this.getDuration() + 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

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
        }
        this.startPeriod = startPeriod;
        // calculate and cache periods that are occupied by this new task assignment
        if (startPeriod == null) {
            this.finalPeriod = null;
            return;
        }
        this.finalPeriod = Period.get(this.getStartPeriod().getId() + this.getDuration() - 1);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Task [id=").append(this.id).append(", ");
        if (this.earliestStart != null) {
            builder.append("earliestStart=").append(this.earliestStart).append(", ");
        }
        if (this.dueBy != null) {
            builder.append("dueBy=").append(this.dueBy).append(", ");
        }
        builder.append("duration=").append(this.duration).append(", ");
        if (this.powerConsumption != null) {
            builder.append("powerConsumption=").append(this.powerConsumption).append(", ");
        }
        if (this.resourceConsumption != null) {
            builder.append("resourceConsumption=").append(this.resourceConsumption).append(", ");
        }
        if (this.startPeriod != null) {
            builder.append("startPeriod=").append(this.startPeriod).append(", ");
        }
        if (this.finalPeriod != null) {
            builder.append("finalPeriod=").append(this.finalPeriod).append(", ");
        }
        if (this.executor != null) {
            builder.append("executor=").append(this.executor).append(", ");
        }
        builder.append("mayShutdownOnCompletion=").append(this.mayShutdownOnCompletion).append("]");
        return builder.toString();
    }

}
