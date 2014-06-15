package org.optaplanner.examples.icon.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Machine {

    private final Map<Resource, MachineCapacity> capacities = new HashMap<Resource, MachineCapacity>();

    private final BigDecimal costOnShutdown;

    private final BigDecimal costOnStartup;

    private final BigDecimal costWhenIdle;

    private final int id;

    public Machine(final int id, final BigDecimal costIdle, final BigDecimal costUp, final BigDecimal costDown, final List<Integer> resourceCapacity) {
        this.id = id;
        this.costWhenIdle = costIdle;
        this.costOnStartup = costUp;
        this.costOnShutdown = costDown;
        for (int i = 0; i < resourceCapacity.size(); i++) {
            final Resource r = Resource.get(i);
            this.capacities.put(r, new MachineCapacity(this, r, resourceCapacity.get(i)));
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

    public MachineCapacity getCapacity(final Resource resource) {
        return this.capacities.get(resource);
    }

    public BigDecimal getCostOnShutdown() {
        return this.costOnShutdown;
    }

    public BigDecimal getCostOnStartup() {
        return this.costOnStartup;
    }

    public BigDecimal getCostWhenIdle() {
        return this.costWhenIdle;
    }

    public int getId() {
        return this.id;
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
