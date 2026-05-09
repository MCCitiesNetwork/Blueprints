package io.github.bl3rune.blueprints.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    /**
     * Translates the positionString to Location
     * 
     * @param positionString location in format world:X:Y:Z
     * @return org.bukkit.Location
     */
    public static Location getCoordsFromPosString(String positionString) {
        String[] pos = EncodingUtils.modifierSplit(positionString);
        return new Location(Bukkit.getWorld(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]), Integer.parseInt(pos[3]));
    }

    public static String locationStringFormat(Location location) {
        return String.format("%s:%d:%d:%d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static int[] reorderNegativeCoords(int pos1, int pos2) {
        if (pos1 > pos2) {
            return new int[] { pos2, pos1 };
        } else {
            return new int[] { pos1, pos2 };
        }
    }

    public static boolean locationsMatch(Location location1, Location location2) {
        if (!location1.getWorld().getName().equals(location2.getWorld().getName())) return false;
        if (location1.getBlockX() != location2.getBlockX()) return false;
        if (location1.getBlockY() != location2.getBlockY()) return false;
        if (location1.getBlockZ() != location2.getBlockZ()) return false;
        return true;
    }

}
