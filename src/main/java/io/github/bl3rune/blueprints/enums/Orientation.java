package io.github.bl3rune.blueprints.enums;

import java.util.stream.Stream;

import org.bukkit.block.BlockFace;

/**
 * 3D orientation the camera is facing towards
 * based on org.bukkit.block.BlockFace
 * 
 * WEST = X- Z- Y+ EAST = X+ Z+ Y+
 * DOWN = Y- X+ Z- UP = Y+ X- Z+
 * NORTH = Z- X+ Y+ SOUTH = Z+ X- Y+
 */
public enum Orientation {

    EAST (
        BlockFace.EAST,
        12,
        Direction.X_POS,
        Direction.Y_POS,
        Direction.Z_POS
    ),
    WEST (
        BlockFace.WEST,
        4,
        Direction.X_NEG,
        Direction.Y_POS,
        Direction.Z_NEG
    ),
    NORTH (
        BlockFace.NORTH,
        8,
        Direction.Z_NEG,
        Direction.Y_POS,
        Direction.X_POS
    ),
    SOUTH (
        BlockFace.SOUTH,
        0,
        Direction.Z_POS,
        Direction.Y_POS,
        Direction.X_NEG
    ),
    UP (
        BlockFace.UP,
        0,
        Direction.X_POS,
        Direction.Z_POS,
        Direction.Y_POS
    ),
    DOWN (
        BlockFace.DOWN,
        0,
        Direction.X_NEG,
        Direction.Z_NEG,
        Direction.Y_NEG
    );

    private final BlockFace blockFace;
    private final String description;
    private final String directional;
    private final String directionalShort;
    private final String rotatable;
    private final String rotatableShort;
    private final Direction outer;
    private final Direction middle;
    private final Direction inner;

    Orientation(BlockFace bf, int rotatable, Direction outer, Direction middle, Direction inner) {
        this.blockFace = getCartesianBlockFace(bf);
        this.description = this.blockFace.name().substring(0, 1);
        this.directional = "facing=" + this.blockFace.name().toLowerCase();
        this.directionalShort = "f=" + this.description.toLowerCase();
        this.rotatable = "rotation=" + Integer.toString(rotatable);
        this.rotatableShort = "r=" + Integer.toString(rotatable);
        this.outer = outer;
        this.middle = middle;
        this.inner = inner;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public String getDescription() {
        return description;
    }

    public String getDirectional() {
        return directional;
    }

    public String getDirectionalShort() {
        return directionalShort;
    }

    public String getRotatable() {
        return rotatable;
    }
     
    public String getRotatableShort() {
        return rotatableShort;
    }

    public Direction getOuter() {
        return outer;
    }

    /*
     * Applies roatation to the directions based on these rules:
     * - ROTATION TOP = (outer) (middle) (inner)
     * - ROTATION RIGHT = (outer) -(inner) (middle)
     * - ROTATION BOTTOM = (outer) -(middle) -(inner)
     * - ROTATION LEFT = (outer) (inner) -(middle)
     */

    public Direction getMiddle(Rotation r) {
        if (r.isHorizontal()) {
            return r == Rotation.RIGHT ? inner : inner.getInverted();
        }
        return r == Rotation.TOP ? middle : middle.getInverted();
    }

    public Direction getInner(Rotation r) {
        if (r.isHorizontal()) {
            return r == Rotation.LEFT ? middle : middle.getInverted();
        }
        return r == Rotation.TOP ? inner : inner.getInverted();
    } 

    public Orientation getOpposite() {
        BlockFace blockFace = getCartesianBlockFace(getBlockFace());
        return getOrientation(blockFace.getOppositeFace());
    }

    public boolean isCompass() {
        if (this == UP || this == DOWN) {
            return false;
        }
        return true;
    }

    public Orientation getNextCompass() {
        switch(this) {
            case NORTH:
                return Orientation.EAST;
            case EAST:
                return Orientation.SOUTH;
            case SOUTH:
                return Orientation.WEST;
            case WEST:
                return Orientation.NORTH;
            default:
               return null;
        }
    }

    public Orientation getNextOrientation() {
        switch (this) {
            case DOWN:
                return Orientation.NORTH;
            case EAST:
                return Orientation.SOUTH;
            case NORTH:
                return Orientation.EAST;
            case SOUTH:
            default:
                return Orientation.WEST;
            case UP:
                return Orientation.DOWN;
            case WEST:
                return Orientation.UP;
        }
    }

    public static Orientation findOrientationInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.getDirectional().equalsIgnoreCase(s) || o.getDirectionalShort().equalsIgnoreCase(s)) {
                return o;
            }
        }
        return null;
    }

    public static Orientation findRotatableInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.getRotatable().equalsIgnoreCase(s) || o.getRotatableShort().equalsIgnoreCase(s)) {
                return o;
            }
        }
        return null;
    }

    public static Orientation findMultiFacingInComplexDataString(String s) {
        for (Orientation o : Orientation.values()) {
            if (o.isCompass() && s.equalsIgnoreCase(o.name() + "=true")) {
                return o;
            }
        }
        return null;
    }

    public static Orientation getOrientation(String description) {
        return Stream.of(Orientation.values()).filter(o -> o.getDescription().equals(description))
                .findFirst().orElse(SOUTH);
    }

    public static Orientation getOrientation(BlockFace bf) {
        BlockFace blockFace = getCartesianBlockFace(bf);
        return Stream.of(Orientation.values()).filter(o -> o.getBlockFace().equals(blockFace))
                .findFirst().orElse(SOUTH);
    }

    public static BlockFace getCartesianBlockFace(BlockFace bf) {
        switch (bf) {
            case UP:
            case DOWN:
            case EAST:
            case WEST:
            case NORTH:
            case SOUTH:
                return bf;
            case EAST_NORTH_EAST:
            case EAST_SOUTH_EAST:
                return BlockFace.EAST;
            case WEST_NORTH_WEST:
            case WEST_SOUTH_WEST:
                return BlockFace.WEST;
            case NORTH_EAST:
            case NORTH_NORTH_EAST:
            case NORTH_NORTH_WEST:
            case NORTH_WEST:
                return BlockFace.NORTH;
            case SOUTH_EAST:
            case SOUTH_SOUTH_EAST:
            case SOUTH_SOUTH_WEST:
            case SOUTH_WEST:
            case SELF:
            default:
                return BlockFace.SOUTH;
        }
    }

}
