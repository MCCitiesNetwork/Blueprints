package io.github.bl3rune.blueprints.enums;

import org.bukkit.block.data.type.Slab.Type;

public enum SlabType {

    TOP(Type.TOP),
    BOTTOM(Type.BOTTOM),
    DOUBLE(Type.DOUBLE);

    private Type type;
    private String fullName;
    private String shortName;

    private SlabType(Type type) {
        this.type = type;
        this.fullName = "type=" + type.name().toLowerCase();
        this.shortName = "ty=" + type.name().substring(0, 1).toLowerCase();
    }

    public Type getType() {
        return type;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public static SlabType fromType(Type type) {
        for (SlabType slab : values()) {
            if (slab.getType().equals(type)) {
                return slab;
            }
        }
        return null;
    }

    public static SlabType findInComplexDataString(String s) {
        for (SlabType ax : SlabType.values()) {
            if (ax.getFullName().equalsIgnoreCase(s) || ax.getShortName().equalsIgnoreCase(s)) {
                return ax;
            }
        }
        return null;
    }

}
