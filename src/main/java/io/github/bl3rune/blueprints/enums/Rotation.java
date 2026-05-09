package io.github.bl3rune.blueprints.enums;

import java.util.stream.Stream;

public enum Rotation {

    TOP(0), // 0°
    RIGHT(1), // 90°
    BOTTOM(2), // 180°
    LEFT(3); // 270°

    private final int index;

    Rotation(int i) {
        this.index = i;
    }

    public static Rotation getRotation(int index) {
        int rem = index % 4;
        switch (rem) {
            default: return TOP;
            case 1: return RIGHT;
            case 2: return BOTTOM;
            case 3: return LEFT;
        }
    }

    public int getIndex() {
        return index;
    }

    public String getCode() {
        return Integer.toString(index);
    }

    public boolean isHorizontal() {
        return index % 2 > 0;
    }

    public Rotation getOpposite() {
        return getRotation(index + 2);
    }

    public Rotation getNextRotation() {
        return getRotation(index + 1);
    }

    public static Rotation fromCode(String code) {
        return Stream.of(Rotation.values()).filter(r -> r.getCode().equals(code))
                .findFirst().orElse(Rotation.TOP);
    }
}
