package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Machine {

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Machine [id=").append(this.id).append(", ");
        if (this.costWhenIdle != null) {
            builder.append("costWhenIdle=").append(this.costWhenIdle).append(", ");
        }
        if (this.costOnStartup != null) {
            builder.append("costOnStartup=").append(this.costOnStartup).append(", ");
        }
        if (this.costOnShutdown != null) {
            builder.append("costOnShutdown=").append(this.costOnShutdown).append(", ");
        }
        if (this.capacities != null) {
            builder.append("capacities=").append(Arrays.toString(this.capacities));
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
        if (!(obj instanceof Machine)) {
            return false;
        }
        final Machine other = (Machine) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public int getId() {
        return this.id;
    }

    public BigDecimal getCostWhenIdle() {
        return this.costWhenIdle;
    }

    public BigDecimal getCostOnStartup() {
        return this.costOnStartup;
    }

    public BigDecimal getCostOnShutdown() {
        return this.costOnShutdown;
    }

    private final int id;
    private final BigDecimal costWhenIdle;
    private final BigDecimal costOnStartup;
    private final BigDecimal costOnShutdown;
    private final int[] capacities;

    public Machine(final int id, final BigDecimal costIdle, final BigDecimal costUp, final BigDecimal costDown, final List<Integer> resourceCapacity) {
        this.id = id;
        this.costWhenIdle = costIdle;
        this.costOnStartup = costUp;
        this.costOnShutdown = costDown;
        this.capacities = new int[resourceCapacity.size()];
        int i = 0;
        for (final Integer capacity : resourceCapacity) {
            this.capacities[i] = capacity;
            i++;
        }
    }

    public int getResourceCapacity(final int resourceId) {
        return this.capacities[resourceId];
    }

}
