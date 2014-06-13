package org.optaplanner.examples.icon.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;

@PlanningSolution
public class Schedule implements Solution<BendableScore> {

    private Forecast forecast;

    private Set<Machine> machines;

    private int resolution;

    private int resourceCount;

    private BendableScore score;

    private Set<Task> tasks;

    protected Schedule() {
        // FIXME planner cloning prevents immutability
    }

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

    @ValueRangeProvider(id = "possibleExecutorRange")
    public Set<Machine> getMachines() {
        return this.machines;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        return Collections.emptySet();
    }

    public int getResolution() {
        return this.resolution;
    }

    public int getResourceCount() {
        return this.resourceCount;
    }

    @Override
    public BendableScore getScore() {
        return this.score;
    }

    @PlanningEntityCollectionProperty
    public Set<Task> getTasks() {
        return this.tasks;
    }

    @Override
    public void setScore(final BendableScore score) {
        this.score = score;
    }

}
