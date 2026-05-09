package io.github.bl3rune.blueprints.services.domain;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.Orientation;

/**
 * Pure placement planning: given a position descriptor, an anchor location,
 * the player facing/alignment configuration, and whether placement is on top
 * of the clicked block, produce the function that maps grid coordinates to
 * world coordinates. Behavior matches the original
 * {@code BlueprintData.buildCalculateFinalLocationFunction} exactly.
 */
public final class PlacementPlanner {

    public Function<Location, Location> buildCalculateFinalLocationFunction(Player player,
            Location location, boolean onTop, ManipulatablePosition position) {
        final Alignment align = GlobalConfig.getAlignment();
        final boolean relative = GlobalConfig.getRelativePlacement();
        final BlockFace playerFacing = Orientation.getCartesianBlockFace(player.getFacing());
        Double x = location.getX();
        Double y = location.getY() + (onTop ? 1 : 0);
        Double z = location.getZ();
        double xSize = position.getXSize();
        double xSizeScaled = xSize * position.getScale();

        if (relative) {
            switch (playerFacing) {
                case NORTH:
                    if (xSize > 1 && align == Alignment.LEFT) {
                        x = x + xSizeScaled - 1;
                    } else if (xSize > 1 && align == Alignment.CENTER) {
                        x = x + (xSizeScaled / 2);
                    }
                    break;
                case EAST:
                    if (xSize > 1 && align == Alignment.LEFT) {
                        z = z + xSizeScaled - 1;
                    } else if (xSize > 1 && align == Alignment.CENTER) {
                        z = z + (xSizeScaled / 2);
                    }
                    break;
                case SOUTH:
                default:
                    if (xSize > 1 && align == Alignment.LEFT) {
                        x = x - xSizeScaled + 1;
                    } else if (xSize > 1 && align == Alignment.CENTER) {
                        x = x - (xSizeScaled / 2);
                    }
                    break;
                case WEST:
                    if (xSize > 1 && align == Alignment.LEFT) {
                        z = z - xSizeScaled + 1;
                    } else if (xSize > 1 && align == Alignment.CENTER) {
                        z = z - (xSizeScaled / 2);
                    }
            }
        } else {
            if (xSize > 1 && align == Alignment.LEFT) {
                x = x - xSizeScaled;
            } else if (xSize > 1 && align == Alignment.CENTER) {
                x = x - (xSizeScaled / 2);
            }
        }

        final double xx = x.doubleValue();
        final double yy = y.doubleValue();
        final double zz = z.doubleValue();

        return (Location coords) -> {
            Double cx = coords.getX();
            Double cy = coords.getY() + yy;
            Double cz = coords.getZ();

            if (relative) {
                switch (playerFacing) {
                    case NORTH:
                        cx = xx - cx - 0.5;
                        cz = zz - cz;
                        break;
                    case EAST:
                        double ex = xx + cz;
                        double ez = zz - cx - 0.5;
                        cx = ex;
                        cz = ez;
                        break;
                    case SOUTH:
                    default:
                        cx = xx + cx;
                        cz = zz + cz;
                        break;
                    case WEST:
                        double wx = xx - cz;
                        double wz = zz + cx;
                        cx = wx;
                        cz = wz;
                }
            } else {
                cx = xx + cx;
                cz = zz + cz;
            }

            return new Location(location.getWorld(), cx.intValue(), cy.intValue(), cz.intValue());
        };
    }
}
