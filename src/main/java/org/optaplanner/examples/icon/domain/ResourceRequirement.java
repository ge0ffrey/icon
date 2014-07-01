package org.optaplanner.examples.icon.domain;

public final class ResourceRequirement {

    private final int requirement;

    private final Resource resource;

    public ResourceRequirement(final Resource r, final int requirement) {
        this.resource = r;
        this.requirement = requirement;
    }

    public int getRequirement() {
        return this.requirement;
    }

    public Resource getResource() {
        return this.resource;
    }

}
