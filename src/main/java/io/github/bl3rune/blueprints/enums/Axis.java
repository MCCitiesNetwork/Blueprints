package io.github.bl3rune.blueprints.enums;

public enum Axis {
    X(org.bukkit.Axis.X),
    Y(org.bukkit.Axis.Y),
    Z(org.bukkit.Axis.Z);

    private org.bukkit.Axis axis;
    private String fullName;
    private String shortName;

    private Axis(org.bukkit.Axis axis) {
        this.axis = axis;
        this.fullName = "axis=" + this.name().toLowerCase();
        this.shortName = "a=" + this.name().toLowerCase().substring(0, 1);
    }

    public org.bukkit.Axis getAxis() {
        return axis;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public static Axis fromBukkit(org.bukkit.Axis axis) {
        for (Axis face : Axis.values()) {
            if (face.getAxis() == axis) {
                return face;
            }
        }
        return null;
    }

    public static Axis findInComplexDataString(String s) {
        for (Axis ax : Axis.values()) {
            if (ax.getFullName().equalsIgnoreCase(s) || ax.getShortName().equalsIgnoreCase(s)) {
                return ax;
            }
        }
        return null;
    }

}
