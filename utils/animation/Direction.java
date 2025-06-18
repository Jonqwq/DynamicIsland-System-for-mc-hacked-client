package org.eris.utils.render.animation;

public enum Direction {
    FORWARDS,
    BACKWARDS;

    public Direction opposite() {
        if (this == FORWARDS) {
            return BACKWARDS;
        }
        return FORWARDS;
    }

    public boolean forwards() {
        return this == FORWARDS;
    }

    public boolean backwards() {
        return this == BACKWARDS;
    }
}
