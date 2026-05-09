package io.github.bl3rune.blueprints.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.data.MaterialData;
import io.github.bl3rune.blueprints.enums.Orientation;

public class EdgeCaseBlockUtils {

    public static boolean isEdgeCaseBlock(MaterialData materialData) {
        // Check if the block is a specific type or has certain properties
        if (Tag.BEDS.isTagged(materialData.getMaterial())) {
            return true;
        }
        return false;
    }

    public static int getEdgeCaseIngredientCountToAdd(MaterialData materialData) {
        Material material = materialData.getMaterial();
        if (Tag.BEDS.isTagged(material)) {
            if (materialData.getComplexData().contains("part=foot")) {
                return 0;
            }
        }
        if (Tag.DOORS.isTagged(material)) {
            if (materialData.getComplexData().contains("half=upper")) {
                return 0;
            }
        }
        return materialData.getCount();
    }

    public static void handleEdgeCasePlacement(Player player, Location location, MaterialData materialData) {
        // Handle the edge cases
        Material material = materialData.getMaterial();
        if (Tag.BEDS.isTagged(material)) {
            handleBedPlacement(player, location, materialData);
        }
        if (Tag.DOORS.isTagged(material)) {
            handleDoorPlacement(player, location, materialData);
        }
        return;
    }

    private static void handleBedPlacement(Player player, Location location, MaterialData materialData) {
        if (materialData.getComplexData().contains("part=foot")) {
            return;
        }
        
        Block block = location.getBlock();
        Orientation o = materialData.getFace();

        if (GlobalConfig.getRelativePlacement()) {
            BlockFace playerFacing = Orientation.getCartesianBlockFace(player.getFacing());
            switch (playerFacing) {
                case EAST:
                    o = o.getOpposite().getNextCompass();
                    break;
                case NORTH:
                    o = o.getOpposite();
                    break;
                case WEST:
                    o = o.getNextCompass();
                    break;
                default:
                    break;
            }

        }
        if (o == null || !o.isCompass()) {
            player.sendMessage(ChatColor.RED + "Bed is not flat, cannot place");
            return;
        }
        final BlockFace facing = o.getBlockFace();
        for (Bed.Part part : Bed.Part.values()) {
            block.setBlockData(Bukkit.createBlockData(materialData.getMaterial(), (data) -> {
                ((Bed) data).setPart(part);
                ((Bed) data).setFacing(facing);
            }));
            block = block.getRelative(facing.getOppositeFace());
        }
    }

    private static void handleDoorPlacement(Player player, Location location, MaterialData materialData) {
        if (materialData.getComplexData().contains("half=lower")) {
            return;
        }
        Block block = location.getBlock();
        Orientation o = materialData.getFace();
        if (!o.isCompass()) {
            player.sendMessage(ChatColor.RED + "Door is not vertical, cannot place");
            return;
        }
        final BlockFace facing = o.getBlockFace();
        String cData = materialData.getComplexData();
        for (Bisected.Half part : Bisected.Half.values()) {
            block.setBlockData(Bukkit.createBlockData(materialData.getMaterial(), (data) -> {
                ((Door) data).setFacing(facing);
                ((Door) data).setHalf(part);
                ((Door) data).setHinge(cData.contains("hinge=right") ? Hinge.RIGHT : Hinge.LEFT);
            }));
            block = block.getRelative(BlockFace.DOWN);
        }
    }

}
