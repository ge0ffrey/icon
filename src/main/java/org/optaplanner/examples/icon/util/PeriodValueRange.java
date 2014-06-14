package org.optaplanner.examples.icon.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.optaplanner.core.impl.domain.valuerange.AbstractCountableValueRange;
import org.optaplanner.core.impl.domain.valuerange.util.ValueRangeIterator;
import org.optaplanner.core.impl.solver.random.RandomUtils;
import org.optaplanner.examples.icon.domain.Period;

public final class PeriodValueRange extends AbstractCountableValueRange<Period> {

    private class OriginalPeriodValueRangeIterator extends ValueRangeIterator<Period> {

        private int upcoming = PeriodValueRange.this.fromInclusive;

        @Override
        public boolean hasNext() {
            return this.upcoming < PeriodValueRange.this.toExclusive;
        }

        @Override
        public Period next() {
            if (this.upcoming >= PeriodValueRange.this.toExclusive) {
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
            final int value = (int) (index * PeriodValueRange.this.incrementUnit + PeriodValueRange.this.fromInclusive);
            return Period.get(value);
        }

    }

    /**
     *
     */
    private static final long serialVersionUID = 633727612190475329L;

    private final int fromInclusive, toExclusive;

    private final int incrementUnit = 1;

    public PeriodValueRange(final int fromInclusive, final int toExclusive) {
        if (fromInclusive >= toExclusive) {
            throw new IllegalArgumentException("Left must be smaller than right.");
        } else if (fromInclusive < 0) {
            throw new IllegalArgumentException("Left must be 0 or more.");
        }
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
    }

    @Override
    public boolean contains(final Period value) {
        final int id = value.getId();
        return (id >= this.fromInclusive && id <= this.toExclusive);
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
        return Period.get((int) (this.fromInclusive + index));
    }

    @Override
    public long getSize() {
        return this.toExclusive - this.fromInclusive + 1;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PeriodValueRange [from=").append(this.fromInclusive).append(", to=").append(this.toExclusive).append(", incrementUnit=").append(this.incrementUnit).append("]");
        return builder.toString();
    }
}