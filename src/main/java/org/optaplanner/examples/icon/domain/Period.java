package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

public class Period implements Comparable<Period> {

    private static final Int2ObjectSortedMap<Period> PERIODS = new Int2ObjectRBTreeMap<Period>();

    public synchronized static Period get(final int id) {
        if (id < 0) {
            throw new IllegalStateException("Periods start at 0, you asked for: " + id);
        }
        Period p = Period.PERIODS.get(id);
        if (p == null) {
            p = new Period(id);
            Period.PERIODS.put(id, p);
        }
        return p;
    }

    private final int id;

    private Period next, previous;

    private Period(final int id) {
        this.id = id;
    }

    @Override
    public int compareTo(final Period o) {
        if (o == this) {
            return 0;
        } else if (o.id > this.id) {
            return -1;
        } else {
            return 1;
        }
    }

    public int getId() {
        return this.id;
    }

    public Period next() {
        if (this.next == null) {
            this.next = Period.get(this.id + 1);
        }
        return this.next;
    }

    public Period previous() {
        if (this.id == 0) {
            throw new IllegalStateException("No previous period.");
        } else if (this.previous == null) {
            this.previous = Period.get(this.id - 1);
        }
        return this.previous;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Period [id=").append(this.id).append("]");
        return builder.toString();
    }

}
