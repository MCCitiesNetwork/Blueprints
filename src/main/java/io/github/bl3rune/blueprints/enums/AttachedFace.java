package io.github.bl3rune.blueprints.enums;

import org.bukkit.block.data.FaceAttachable;

public enum AttachedFace {

    CEILING(FaceAttachable.AttachedFace.CEILING),
    WALL(FaceAttachable.AttachedFace.WALL),
    FLOOR(FaceAttachable.AttachedFace.FLOOR);

    private FaceAttachable.AttachedFace attachedFace;
    private String fullName;
    private String shortName;

    private AttachedFace(FaceAttachable.AttachedFace attachedFace) {
        this.attachedFace = attachedFace;
        this.fullName = "face=" + this.name().toLowerCase();
        this.shortName = "fa=" + this.name().toLowerCase().substring(0, 1);
    }

    public FaceAttachable.AttachedFace getAttachedFace() {
        return attachedFace;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public static AttachedFace fromBukkit(FaceAttachable.AttachedFace attachedFace) {
        for (AttachedFace face : AttachedFace.values()) {
            if (face.getAttachedFace() == attachedFace) {
                return face;
            }
        }
        return null;
    }

    public static AttachedFace findInComplexDataString(String s) {
        for (AttachedFace ax : AttachedFace.values()) {
            if (ax.getFullName().equalsIgnoreCase(s) || ax.getShortName().equalsIgnoreCase(s)) {
                return ax;
            }
        }
        return null;
    }

}
