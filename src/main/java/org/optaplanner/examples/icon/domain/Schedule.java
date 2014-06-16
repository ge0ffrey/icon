package org.optaplanner.examples.icon.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;

@PlanningSolution
public class Schedule implements Solution<HardSoftBigDecimalScore> {

    private static final Set<Boolean> SHUTDOWN_POSSIBILITIES = new HashSet<Boolean>();

    static {
        Schedule.SHUTDOWN_POSSIBILITIES.add(false);
        Schedule.SHUTDOWN_POSSIBILITIES.add(true);
    }

    private Forecast forecast;

    private Set<Machine> machines;

    private int resolution;

    private int resourceCount;

    private HardSoftBigDecimalScore score;

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
        final Collection<Object> facts = new LinkedList<Object>();
        facts.addAll(this.getForecast().getAll());
        facts.addAll(this.getMachines());
        for (int i = 0; i < 1440 / this.getResolution(); i++) {
            facts.add(Period.get(i));
        }
        for (int i = 0; i < this.getResourceCount(); i++) {
            final Resource r = Resource.get(i);
            facts.add(r);
            for (final Machine m : this.getMachines()) {
                facts.add(m.getCapacity(r));
            }
        }
        return facts;
    }

    public int getResolution() {
        return this.resolution;
    }

    public int getResourceCount() {
        return this.resourceCount;
    }

    @Override
    public HardSoftBigDecimalScore getScore() {
        return this.score;
    }

    @ValueRangeProvider(id = "possibleShutdownRange")
    public Set<Boolean> getShutdownPossibilities() {
        return Schedule.SHUTDOWN_POSSIBILITIES;
    }

    @PlanningEntityCollectionProperty
    public Set<Task> getTasks() {
        return this.tasks;
    }

    @Override
    public void setScore(final HardSoftBigDecimalScore score) {
        this.score = score;
    }

}
