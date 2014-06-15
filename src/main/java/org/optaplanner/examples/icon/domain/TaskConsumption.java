package org.optaplanner.examples.icon.domain;

public class TaskConsumption {

    private final int consumption;

    private final Resource resource;

    private final Task task;

    TaskConsumption(final Task t, final Resource r, final int consumption) {
        this.task = t;
        this.resource = r;
        this.consumption = consumption;
    }

    public int getConsumption() {
        return this.consumption;
    }

    public Resource getResource() {
        return this.resource;
    }

    public Task getTask() {
        return this.task;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TaskConsumption [consumption=").append(this.consumption).append(", ");
        if (this.task != null) {
            builder.append("task=").append(this.task.getId()).append(", ");
        }
        if (this.resource != null) {
            builder.append("resource=").append(this.resource);
        }
        builder.append("]");
        return builder.toString();
    }

}
