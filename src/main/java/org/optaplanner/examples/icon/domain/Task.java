package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.math.BigDecimal;
import java.util.List;

import org.optaplanner.examples.icon.util.PeriodValueRange;

public class Task {

    private final PeriodValueRange availableStartPeriodRange;
    private final int duration;
    private final Period earliestStart;
    private final int id;
    private final Period latestEnd;
    private final BigDecimal powerConsumption;
    private final Object2IntMap<Resource> resourceConsumption = new Object2IntOpenHashMap<Resource>();

    public Task(final int id, final int duration, final int earliestStart, final int dueBy, final BigDecimal powerUse, final List<Integer> resourceConsumption) {
        this.id = id;
        if (duration + earliestStart - 1 >= dueBy) {
            throw new IllegalStateException("Task " + id + " has wrong duration " + duration + ". Starts at " + earliestStart + " and yet must end before " + dueBy + ".");
        }
        this.duration = duration;
        this.earliestStart = Period.get(earliestStart);
        this.latestEnd = Period.get(dueBy - 1); // exclusive to inclusive
        this.powerConsumption = powerUse;
        for (int i = 0; i < resourceConsumption.size(); i++) {
            this.resourceConsumption.put(Resource.get(i), resourceConsumption.get(i));
        }
        this.availableStartPeriodRange = new PeriodValueRange(this.getEarliestStart().getId(), this.getLatestEnd().getId() - this.getDuration() + 2);
    }

    public PeriodValueRange getAvailableStartPeriodRange() {
        return this.availableStartPeriodRange;
    }

    public int getConsumption(final Resource resource) {
        return this.resourceConsumption.getInt(resource);
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

    public BigDecimal getPowerConsumption() {
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
        if (this.powerConsumption != null) {
            builder.append("powerConsumption=").append(this.powerConsumption).append(", ");
        }
        if (this.resourceConsumption != null) {
            builder.append("resourceConsumption=").append(this.resourceConsumption);
        }
        builder.append("]");
        return builder.toString();
    }

}
