package org.optaplanner.examples.icon.domain;

import java.util.Collections;
import java.util.Set;

public class Schedule {

    private final Forecast forecast;

    private final Set<Machine> machines;

    private final int resolution;

    private final int resourceCount;

    private final Set<Task> tasks;

    public Schedule(final int resolution, final int resourceCount, final Set<Machine> machines, final Set<Task> tasks, final Forecast forecast) {
        this.resolution = resolution;
        this.resourceCount = resourceCount;
        this.machines = Collections.unmodifiableSet(machines);
        this.tasks = Collections.unmodifiableSet(tasks);
        this.forecast = forecast;
    }

    public Forecast getForecast() {
        return this.forecast;
    }

    public Set<Machine> getMachines() {
        return this.machines;
    }

    public int getResolution() {
        return this.resolution;
    }

    public int getResourceCount() {
        return this.resourceCount;
    }

    public Set<Task> getTasks() {
        return this.tasks;
    }

}
