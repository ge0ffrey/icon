package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Task {

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
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

    public int getId() {
        return this.id;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getEarliestStart() {
        return this.earliestStart;
    }

    public int getDueBy() {
        return this.dueBy;
    }

    public BigDecimal getPowerConsumption() {
        return this.powerConsumption;
    }

    private final int id;
    private final int duration;
    private final int earliestStart;
    private final int dueBy;
    private final BigDecimal powerConsumption;
    private final int[] resourceConsumption;

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

    public int getResourceConsumption(final int resourceId) {
        return this.resourceConsumption[resourceId];
    }

}
