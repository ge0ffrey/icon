package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.math.BigDecimal;
import java.util.List;

public class Machine {

    private final Object2IntMap<Resource> capacities = new Object2IntOpenHashMap<Resource>();

    private final double costOnShutdown;

    private final double costOnStartup;

    private final double costWhenIdle;

    private final int id;

    public Machine(final int id, final BigDecimal costIdle, final BigDecimal costUp, final BigDecimal costDown, final List<Integer> resourceCapacity) {
        this.id = id;
        this.costWhenIdle = costIdle.doubleValue();
        this.costOnStartup = costUp.doubleValue();
        this.costOnShutdown = costDown.doubleValue();
        for (int i = 0; i < resourceCapacity.size(); i++) {
            this.capacities.put(Resource.get(i), resourceCapacity.get(i));
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
        if (!(obj instanceof Machine)) {
            return false;
        }
        final Machine other = (Machine) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public double getCostOnShutdown() {
        return this.costOnShutdown;
    }

    public double getCostOnStartup() {
        return this.costOnStartup;
    }

    public double getCostWhenIdle() {
        return this.costWhenIdle;
    }

    public int getId() {
        return this.id;
    }

    public int getResourceCapacity(final Resource resource) {
        return this.capacities.get(resource);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Machine [id=").append(this.id).append(", ");
        builder.append("costOnStartup=").append(this.costOnStartup).append(", ");
        builder.append("costOnShutdown=").append(this.costOnShutdown).append(", ");
        builder.append("costWhenIdle=").append(this.costWhenIdle).append(", ");
        if (this.capacities != null) {
            builder.append("capacities=").append(this.capacities);
        }
        builder.append("]");
        return builder.toString();
    }

}
