package org.optaplanner.examples.icon.domain;

public class MachineCapacity {

    private final int capacity;

    private final Machine machine;

    private final Resource resource;

    MachineCapacity(final Machine m, final Resource r, final int capacity) {
        this.machine = m;
        this.resource = r;
        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public Resource getResource() {
        return this.resource;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MachineCapacity [capacity=").append(this.capacity).append(", ");
        if (this.machine != null) {
            builder.append("machine=").append(this.machine.getId()).append(", ");
        }
        if (this.resource != null) {
            builder.append("resource=").append(this.resource);
        }
        builder.append("]");
        return builder.toString();
    }

}
