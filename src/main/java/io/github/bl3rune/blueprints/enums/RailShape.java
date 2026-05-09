package io.github.bl3rune.blueprints.enums;

import org.bukkit.block.data.Rail;

public enum RailShape {

    NORTH_SOUTH(Rail.Shape.NORTH_SOUTH),
    EAST_WEST(Rail.Shape.EAST_WEST),
    ASCENDING_EAST(Rail.Shape.ASCENDING_EAST),
    ASCENDING_WEST(Rail.Shape.ASCENDING_WEST),
    ASCENDING_NORTH(Rail.Shape.ASCENDING_NORTH),
    ASCENDING_SOUTH(Rail.Shape.ASCENDING_SOUTH),
    SOUTH_EAST(Rail.Shape.SOUTH_EAST),
    SOUTH_WEST(Rail.Shape.SOUTH_WEST),
    NORTH_WEST(Rail.Shape.NORTH_WEST),
    NORTH_EAST(Rail.Shape.NORTH_EAST);

    private final Rail.Shape shape;
    private final String fullName;
    private final String shortName;
       
    private RailShape(Rail.Shape shape) {
        this.shape = shape;
        this.fullName = "shape=" + shape.name().toLowerCase();
        int underscoreIndex = shape.name().indexOf("_");
        this.shortName = "sh=" + (shape.name().substring(0, 1) 
                + shape.name().substring(underscoreIndex, underscoreIndex + 2)).toLowerCase();

    }

    public Rail.Shape getShape() {
        return shape;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public static RailShape fromBukkit(Rail.Shape shape) {
        for (RailShape railshape : RailShape.values()) {
            if (railshape.getShape() == shape) {
                return railshape;
            }
        }
        return null;
    }
    
    public static RailShape findInComplexDataString(String s) {
        for (RailShape rs : RailShape.values()) {
            if (rs.getFullName().equalsIgnoreCase(s) || rs.getShortName().equalsIgnoreCase(s)) {
                return rs;
            }
        }
        return null;
    }   

}
