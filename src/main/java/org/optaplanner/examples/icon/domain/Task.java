package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.optaplanner.examples.icon.util.FixedPointArithmetic;
import org.optaplanner.examples.icon.util.PeriodValueRange;

public class Task {

    private final Set<Machine> availableMachines;

    private final PeriodValueRange availableStartPeriodRange;

    private final long difficulty;
    private final int duration;
    private final Period earliestStart;
    private final int id;
    private final Period latestEnd;
    private final long powerConsumption;
    private final Int2ObjectMap<ResourceRequirement> resourceConsumption = new Int2ObjectOpenHashMap<ResourceRequirement>();

    public Task(final int id, final int duration, final int earliestStart, final int dueBy, final BigDecimal powerUse, final List<Integer> resourceConsumption, final Collection<Machine> machines) {
        this.id = id;
        if (duration + earliestStart - 1 >= dueBy) {
            throw new IllegalStateException("Task " + id + " has wrong duration " + duration + ". Starts at " + earliestStart + " and yet must end before " + dueBy + ".");
        }
        this.duration = duration;
        this.earliestStart = Period.get(earliestStart);
        this.latestEnd = Period.get(dueBy - 1); // exclusive to inclusive
        this.powerConsumption = FixedPointArithmetic.fromBigDecimal(powerUse);
        final Set<Machine> tmp = new LinkedHashSet<Machine>(machines);
        long difficulty = duration;
        for (int i = 0; i < resourceConsumption.size(); i++) {
            final int consumption = resourceConsumption.get(i);
            if (consumption < 1) {
                continue;
            }
            final Resource r = Resource.get(i);
            this.resourceConsumption.put(i, new ResourceRequirement(r, consumption));
            // cleanse the list of available machines from machines that cannot accommodate this task
            final Iterator<Machine> iter = tmp.iterator();
            while (iter.hasNext()) {
                final Machine m = iter.next();
                if (consumption > m.getCapacity(r).getCapacity()) {
                    iter.remove();
                }
            }
            difficulty *= consumption;
        }
        this.difficulty = difficulty;
        if (tmp.isEmpty()) {
            throw new IllegalStateException("No executors available for " + this);
        }
        this.availableMachines = Collections.unmodifiableSet(tmp);
        this.availableStartPeriodRange = new PeriodValueRange(this.getEarliestStart().getId(), this.getLatestEnd().getId() - this.getDuration() + 2);
    }

    public Set<Machine> getAvailableMachines() {
        return this.availableMachines;
    }

    public PeriodValueRange getAvailableStartPeriodRange() {
        return this.availableStartPeriodRange;
    }

    public int getConsumption(final Resource resource) {
        final int id = resource.getId();
        if (this.resourceConsumption.containsKey(id)) {
            return this.resourceConsumption.get(id).getRequirement();
        } else {
            return 0;
        }
    }

    public Collection<ResourceRequirement> getConsumptions() {
        return this.resourceConsumption.values();
    }

    public long getDifficulty() {
        return this.difficulty;
    }

    public int getDuration() {
        return this.duration;
    }

    public Period getEarliestStart() {
        return this.earliestStart;
    }

    public int getId() {
        return this.id;
    }

    /**
     * Inclusive, as opposed to exclusive specified by the challenge.
     *
     * @return
     */
    public Period getLatestEnd() {
        return this.latestEnd;
    }

    public long getPowerConsumption() {
        return this.powerConsumption;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Task [");
        if (this.availableStartPeriodRange != null) {
            builder.append("availableStartPeriodRange=").append(this.availableStartPeriodRange).append(", ");
        }
        builder.append("duration=").append(this.duration).append(", ");
        if (this.earliestStart != null) {
            builder.append("earliestStart=").append(this.earliestStart).append(", ");
        }
        builder.append("id=").append(this.id).append(", ");
        if (this.latestEnd != null) {
            builder.append("latestEnd=").append(this.latestEnd).append(", ");
        }
        builder.append("powerConsumption=").append(this.powerConsumption).append(", ");
        if (this.resourceConsumption != null) {
            builder.append("resourceConsumption=").append(this.resourceConsumption);
        }
        builder.append("]");
        return builder.toString();
    }

}
