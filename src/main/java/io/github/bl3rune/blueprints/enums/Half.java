package io.github.bl3rune.blueprints.enums;

import org.bukkit.block.data.BlockData;

public enum Half {

    TOP(org.bukkit.block.data.Bisected.Half.TOP),
    BOTTOM(org.bukkit.block.data.Bisected.Half.BOTTOM),
    UPPER(org.bukkit.block.data.Bisected.Half.TOP),
    LOWER(org.bukkit.block.data.Bisected.Half.BOTTOM);

    private org.bukkit.block.data.Bisected.Half half;
    private String fullName;
    private String shortName;

    Half(org.bukkit.block.data.Bisected.Half half) {
        this.half = half;
        this.fullName = "half=" + name().toLowerCase();
        this.shortName = "h=" + name().toLowerCase().substring(0, 1);
    }
    
    public org.bukkit.block.data.Bisected.Half getHalf() {
        return half;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public static Half fromBukkit(BlockData data) {
        for (Half bisected : values()) {
            if (data.getAsString().contains(bisected.getFullName())) {
                return bisected;
            }
        }
        return null;
    }

    public static Half findInComplexDataString(String s) {
        for (Half bisected : values()) {
            if (bisected.getFullName().equalsIgnoreCase(s) || bisected.getShortName().equalsIgnoreCase(s)) {
                return bisected;
            }
        }
        return null;
    }

}
