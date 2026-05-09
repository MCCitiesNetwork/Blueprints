package io.github.bl3rune.blueprints.services.domain;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.data.MaterialData;
import io.github.bl3rune.blueprints.utils.EdgeCaseBlockUtils;

/**
 * Block application strategy: edge-case blocks dispatch to
 * {@link EdgeCaseBlockUtils}, blocks with complex {@code BlockData} go through
 * {@code Bukkit.createBlockData}, and the remainder fall back to a plain
 * {@code setType}. Behavior matches {@code BlueprintData.placeBlock}.
 */
public final class BlockApplicationStrategy {

    private final Logger logger;

    public BlockApplicationStrategy(Logger logger) {
        this.logger = logger;
    }

    public boolean place(Player player, Location location, MaterialData materialData) {
        World world = Bukkit.getWorld(location.getWorld().getName());
        if (world == null) {
            logger.info("World not found: " + location.getWorld().getName());
            return false;
        }

        String complexData = materialData.getComplexData();
        if (EdgeCaseBlockUtils.isEdgeCaseBlock(materialData)) {
            EdgeCaseBlockUtils.handleEdgeCasePlacement(player, location, materialData);
        } else if (complexData != null) {
            try {
                BlockData blockData = Bukkit.createBlockData(complexData);
                world.setBlockData(location, blockData);
            } catch (Exception e) {
                /* Tried to apply invalid block data */
            }
        } else {
            world.setType(location, materialData.getMaterial());
        }
        return true;
    }
}
