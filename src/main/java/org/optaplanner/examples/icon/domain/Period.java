package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import java.util.Collection;

public class Period implements Comparable<Period> {

    private static final Int2ObjectSortedMap<Period> PERIODS = new Int2ObjectRBTreeMap<Period>();

    public synchronized static Period get(final int id) {
        if (id < 0) {
            throw new IllegalStateException("Periods start at 0, you asked for: " + id);
        }
        if (!Period.PERIODS.containsKey(id)) {
            Period.PERIODS.put(id, new Period(id));
        }
        return Period.PERIODS.get(id);
    }

    public synchronized static Collection<Period> getAll() {
        return Period.PERIODS.values();
    }

    private final int id;

    private Period(final int id) {
        this.id = id;
    }

    @Override
    public int compareTo(final Period o) {
        if (o.getId() > this.getId()) {
            return -1;
        } else if (o.getId() == this.getId()) {
            return 0;
        } else {
            return 1;
        }
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Period [id=").append(this.id).append("]");
        return builder.toString();
    }

}
