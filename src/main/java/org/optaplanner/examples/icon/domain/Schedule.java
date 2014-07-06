package org.optaplanner.examples.icon.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class Schedule implements Solution<HardSoftLongScore> {

    private Forecast forecast;

    private Set<Machine> machines;

    private int resolution;

    private int resourceCount;

    private HardSoftLongScore score;

    private Set<TaskAssignment> taskAssignments;

    protected Schedule() {
        // FIXME planner cloning prevents immutability
    }

    public Schedule(final int resolution, final int resourceCount, final Set<Machine> machines, final Set<TaskAssignment> tasks, final Forecast forecast) {
        this.resolution = resolution;
        this.resourceCount = resourceCount;
        this.machines = Collections.unmodifiableSet(new LinkedHashSet<Machine>(machines));
        this.taskAssignments = Collections.unmodifiableSet(new LinkedHashSet<TaskAssignment>(tasks));
        this.forecast = forecast;
    }

    public Forecast getForecast() {
        return this.forecast;
    }

    public Set<Machine> getMachines() {
        return this.machines;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        final Collection<Object> facts = new LinkedList<Object>();
        facts.addAll(this.getMachines());
        for (int i = 0; i < 1440 / this.getResolution(); i++) {
            final Period p = Period.get(i);
            facts.add(p);
            facts.add(this.getForecast().getForPeriod(p));
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
    public HardSoftLongScore getScore() {
        return this.score;
    }

    @PlanningEntityCollectionProperty
    public Set<TaskAssignment> getTaskAssignments() {
        return this.taskAssignments;
    }

    @Override
    public void setScore(final HardSoftLongScore score) {
        this.score = score;
    }

}
