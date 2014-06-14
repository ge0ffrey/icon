package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.domain.valuerange.AbstractCountableValueRange;
import org.optaplanner.core.impl.domain.valuerange.util.ValueRangeIterator;
import org.optaplanner.core.impl.solver.random.RandomUtils;

@PlanningEntity
public class Task {

    private static final class PeriodValueRange extends AbstractCountableValueRange<Period> {

        private class OriginalPeriodValueRangeIterator extends ValueRangeIterator<Period> {

            private int upcoming = PeriodValueRange.this.from;

            @Override
            public boolean hasNext() {
                return this.upcoming < PeriodValueRange.this.to;
            }

            @Override
            public Period next() {
                if (this.upcoming >= PeriodValueRange.this.to) {
                    throw new NoSuchElementException();
                }
                final int next = this.upcoming;
                this.upcoming += PeriodValueRange.this.incrementUnit;
                return Period.get(next);
            }

        }

        private class RandomPeriodValueRangeIterator extends ValueRangeIterator<Period> {

            private final long size = PeriodValueRange.this.getSize();
            private final Random workingRandom;

            public RandomPeriodValueRangeIterator(final Random workingRandom) {
                this.workingRandom = workingRandom;
            }

            @Override
            public boolean hasNext() {
                return this.size > 0L;
            }

            @Override
            public Period next() {
                final long index = RandomUtils.nextLong(this.workingRandom, this.size);
                final int value = (int) (index * PeriodValueRange.this.incrementUnit + PeriodValueRange.this.from);
                return Period.get(value);
            }

        }

        /**
         *
         */
        private static final long serialVersionUID = 633727612190475329L;

        private final int from, to;

        private final int incrementUnit = 1;

        public PeriodValueRange(final int min, final int max) {
            this.from = min;
            this.to = max;
        }

        @Override
        public boolean contains(final Period value) {
            final int id = value.getId();
            return (id >= this.from && id <= this.to);
        }

        @Override
        public Iterator<Period> createOriginalIterator() {
            return new OriginalPeriodValueRangeIterator();
        }

        @Override
        public Iterator<Period> createRandomIterator(final Random workingRandom) {
            return new RandomPeriodValueRangeIterator(workingRandom);
        }

        @Override
        public Period get(final long index) {
            // FIXME types
            return Period.get((int) (this.from + index));
        }

        @Override
        public long getSize() {
            return this.to - this.from + 1;
        }
    }

    // constants
    private Period dueBy;
    private int duration;
    private Period earliestStart;
    private Machine executor;
    private Period finalPeriod;

    private int id;
    private BigDecimal powerConsumption;

    private final Object2IntMap<Resource> resourceConsumption = new Object2IntOpenHashMap<Resource>();

    // variables
    private Period startPeriod;

    protected Task() {
        // FIXME planner cloning prevents immutability
    }

    public Task(final int id, final int duration, final int earliestStart, final int dueBy, final BigDecimal powerUse, final List<Integer> resourceConsumption) {
        this.id = id;
        this.duration = duration;
        this.earliestStart = Period.get(earliestStart);
        this.dueBy = Period.get(dueBy);
        this.powerConsumption = powerUse;
        for (int i = 0; i < resourceConsumption.size(); i++) {
            this.resourceConsumption.put(Resource.get(i), resourceConsumption.get(i));
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
        if (!(obj instanceof Task)) {
            return false;
        }
        final Task other = (Task) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Period getDueBy() {
        return this.dueBy;
    }

    public int getDuration() {
        return this.duration;
    }
    
    private boolean mayShutdownOnCompletion;
    
    @PlanningVariable(valueRangeProviderRefs = {"possibleShutdownRange"})
    public boolean getShutdownPossible() {
        return this.mayShutdownOnCompletion;
    }

    public void setShutdownPossible(final boolean shutdownPossible) {
        this.mayShutdownOnCompletion = shutdownPossible;
    }

    public Period getEarliestStart() {
        return this.earliestStart;
    }
    
    public boolean isInitialized() {
        return this.executor != null && this.startPeriod != null;
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleExecutorRange"})
    public Machine getExecutor() {
        return this.executor;
    }

    // FIXME changes with start period; should be shadow?
    public Period getFinalPeriod() {
        return this.finalPeriod;
    }

    public int getId() {
        return this.id;
    }

    public BigDecimal getPowerConsumption() {
        return this.powerConsumption;
    }

    public int getResourceConsumption(final Resource resource) {
        return this.resourceConsumption.getInt(resource);
    }

    @PlanningVariable(valueRangeProviderRefs = {"possibleStartPeriodRange"})
    public Period getStartPeriod() {
        return this.startPeriod;
    }

    @ValueRangeProvider(id = "possibleStartPeriodRange")
    public ValueRange<Period> getStartPeriodValueRange() {
        return new PeriodValueRange(this.getEarliestStart().getId(), this.getDueBy().getId() - this.getDuration());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

    public void setExecutor(final Machine executor) {
        this.executor = executor;
    }

    public void setStartPeriod(final Period startPeriod) {
        if (this.startPeriod == startPeriod) {
            // no change
            return;
        }
        this.startPeriod = startPeriod;
        // calculate and cache periods that are occupied by this new task assignment
        if (startPeriod == null) {
            this.finalPeriod = null;
            return;
        }
        this.finalPeriod = Period.get(this.getStartPeriod().getId() + this.getDuration() - 1);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Task [id=").append(this.id).append(", dueBy=").append(this.dueBy).append(", duration=").append(this.duration).append(", earliestStart=").append(this.earliestStart).append(", ");
        builder.append("powerConsumption=").append(this.powerConsumption).append(", ");
        if (this.resourceConsumption != null) {
            builder.append("resourceConsumption=").append(this.resourceConsumption).append(", ");
        }
        builder.append("startPeriod=").append(this.startPeriod).append(", ");
        if (this.executor != null) {
            builder.append("executor=").append(this.executor);
        }
        builder.append("]");
        return builder.toString();
    }

}
