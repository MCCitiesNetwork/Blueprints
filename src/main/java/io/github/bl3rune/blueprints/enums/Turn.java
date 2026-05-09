package io.github.bl3rune.blueprints.enums;

public enum Turn {
    UP(0), RIGHT(1),  DOWN(2), LEFT(3);

    private final int index;

    private Turn(int i) {
        index = i;
    }

    public Turn getTurnByIndex(int i) {
        int rem = i % 4;
        switch(rem) {
            default: return UP;
            case 1: return RIGHT;
            case 2: return DOWN;
            case 3: return LEFT;
        }
    }

    public int getIndex() {
        return index;
    }

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    public Turn plusRotation(Rotation rotation) {
        return getTurnByIndex(index + rotation.getIndex());
    }

    public Turn nextDirection() {
        return getTurnByIndex(index + 1);
    }
}