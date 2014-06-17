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
            final int result = (int) (index * PeriodValueRange.this.incrementUnit + PeriodValueRange.this.from);
            return Period.get(result);
        }

    }

    /**
     *
     */
    private static final long serialVersionUID = 633727612190475329L;

    private final int from, to;

    private final int incrementUnit = 1;

    public PeriodValueRange(final int fromInclusive, final int toExclusive) {
        if (fromInclusive >= toExclusive) {
            throw new IllegalArgumentException("Left (" + fromInclusive + ") must be smaller than right (" + toExclusive + ").");
        } else if (fromInclusive < 0) {
            throw new IllegalArgumentException("Left must be 0 or more.");
        }
        this.from = fromInclusive;
        this.to = toExclusive;
    }

    @Override
    public boolean contains(final Period period) {
        if (period == null) {
            return false;
        }
        final int value = period.getId();
        if (value < this.from || value >= this.to) {
            return false;
        }
        return ((long) value - (long) this.from) % this.incrementUnit == 0;
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
        if (index < 0L || index >= this.getSize()) {
            throw new IndexOutOfBoundsException("The index (" + index + ") must be >= 0 and < size ("
                    + this.getSize() + ").");
        }
        return Period.get((int) (index * this.incrementUnit + this.from));
    }

    @Override
    public long getSize() {
        return ((long) this.to - (long) this.from) / this.incrementUnit;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PeriodValueRange [from=").append(this.from).append(", to=").append(this.to).append(", incrementUnit=").append(this.incrementUnit).append("]");
        return builder.toString();
    }
}