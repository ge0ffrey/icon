package org.optaplanner.examples.icon.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

public class Resource {

    private static final Int2ObjectSortedMap<Resource> RESOURCES = new Int2ObjectRBTreeMap<Resource>();

    public synchronized static Resource get(final int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Resource IDs start at 0, you asked for: " + id);
        }
        Resource r = Resource.RESOURCES.get(id);
        if (r == null) {
            r = new Resource(id);
            Resource.RESOURCES.put(id, r);
        }
        return r;
    }

    private final int id;

    private Resource(final int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Resource [id=").append(this.id).append("]");
        return builder.toString();
    }

}
