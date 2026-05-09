package io.github.bl3rune.blueprints.enums;

public enum Direction {
    X_POS(3),
    X_NEG(-3),
    Y_POS(2),
    Y_NEG(-2),
    Z_POS(1),
    Z_NEG(-1);

    private final int index;

    private Direction(int index) {
        this.index = index;
    }

    public int getRaw() {
        return index;
    }

    public boolean isNegative() {
        return index < 0;
    }

    public int getPosIndex() {
        return index < 0 ? (index * -1) - 1 : index - 1;
    }

    public Direction getInverted() {
        for (Direction d : Direction.values()) {
            if (d.getRaw() == -this.index) {
                return d;
            }
        }
        return null;
    }
}
