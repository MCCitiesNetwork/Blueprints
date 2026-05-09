package io.github.bl3rune.blueprints.services.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import io.github.bl3rune.blueprints.config.GlobalConfig;

/**
 * Inventory cost / discount calculator extracted from
 * {@code BlueprintData.checkPlayerHasBLocksInInventory}. Computes which
 * ingredient blocks the player still needs (after creative-mode bypass,
 * sneak-discount, and inventory + storage scans) and optionally consumes
 * them when {@code removeBlocks} is true.
 */
public final class InventoryCostCalculator {

    private final Logger logger;

    public InventoryCostCalculator(Logger logger) {
        this.logger = logger;
    }

    public Map<String, Integer> checkPlayerHasBlocks(Player player, boolean removeBlocks,
            Map<String, Integer> ingredientsCount, Map<String, Integer> blocksUnableToPlace) {
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("blu3print.no-block-cost")) {
            if (removeBlocks && GlobalConfig.isFreePlacementMessageEnabled()) {
                player.sendMessage(ChatColor.GREEN + "Placing Blu3print for free!");
            }
            return new HashMap<>();
        }

        Map<String, Integer> ingCountCopy = new HashMap<>(ingredientsCount);

        boolean forcePlacePenalty = GlobalConfig.getForcePlacePenaltyEnabled();
        if (player.isSneaking()
                && (player.hasPermission("blu3print.force-place-discount") || !forcePlacePenalty)) {
            if (GlobalConfig.isDiscountPlacementMessageEnabled() && forcePlacePenalty) {
                player.sendMessage(ChatColor.GREEN + "Placing Blu3print for discount as blocks in the way!");
            }
            blocksUnableToPlace.forEach((material, amount) -> {
                Integer count = ingCountCopy.getOrDefault(material, 0);
                count = count - amount;
                if (count < 1) {
                    ingCountCopy.remove(material);
                } else {
                    ingCountCopy.put(material, amount);
                }
            });
        }

        Map<Integer, ItemStack> inventoryBlocks = new HashMap<>();
        Map<Integer, ItemStack> storageBlocks = new HashMap<>();
        int inventoryIndex = 0;
        boolean endOfInventory = false;
        Inventory inventory = player.getInventory();

        while (!endOfInventory && inventoryIndex < inventory.getSize()) {
            try {
                ItemStack itemStack = inventory.getItem(inventoryIndex);
                if (itemStack == null || itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
                    inventoryIndex++;
                    continue;
                }
                if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta bsm = (BlockStateMeta) itemStack.getItemMeta();
                    if (bsm.getBlockState() instanceof Container) {
                        storageBlocks.put(inventoryIndex, itemStack);
                    } else {
                        inventoryBlocks.put(inventoryIndex, itemStack);
                    }
                } else {
                    inventoryBlocks.put(inventoryIndex, itemStack);
                }
                inventoryIndex++;
            } catch (Exception e) {
                logger.warning("ERROR checkPlayerHasBLocksInInventory INV SCAN");
                logger.warning(e.getMessage());
                e.printStackTrace();
                endOfInventory = true;
            }
        }

        inventoryBlocks.forEach((k, v) -> {
            String blockName = v.getType().name();
            if (ingCountCopy.containsKey(blockName)) {
                int count = ingCountCopy.get(blockName);
                int stillNeeded = count - v.getAmount();
                if (stillNeeded < 1) {
                    ingCountCopy.remove(blockName);
                    if (removeBlocks) {
                        v.setAmount(v.getAmount() - count);
                        inventory.setItem(k, v);
                    }
                } else {
                    ingCountCopy.put(blockName, count);
                    if (removeBlocks) {
                        inventory.setItem(k, null);
                    }
                }
            }
        });

        if (ingCountCopy.isEmpty()) {
            return ingCountCopy;
        }

        storageBlocks.forEach((k, v) -> {
            BlockStateMeta bsm = (BlockStateMeta) v.getItemMeta();
            Container container = (Container) bsm.getBlockState();
            Inventory containerInventory = container.getInventory();
            Map<Integer, ItemStack> storageInventoryBlocks = new HashMap<>();
            int storageInventoryIndex = 0;
            boolean endOfStorageInventory = false;
            while (!endOfStorageInventory && storageInventoryIndex < containerInventory.getSize()) {
                try {
                    ItemStack itemStack = containerInventory.getItem(storageInventoryIndex);
                    if (itemStack == null || itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
                        storageInventoryIndex++;
                        continue;
                    }
                    storageInventoryBlocks.put(storageInventoryIndex, itemStack);
                    storageInventoryIndex++;
                } catch (Exception e) {
                    logger.warning("ERROR checkPlayerHasBLocksInInventory BLOCK INV SCAN POSITION " + k
                            + " BLOCK " + v.getType().name());
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                    endOfStorageInventory = true;
                }
            }

            storageInventoryBlocks.forEach((ik, iv) -> {
                String blockName = iv.getType().name();
                if (ingCountCopy.containsKey(blockName)) {
                    int count = ingCountCopy.get(blockName);
                    int stillNeeded = count - iv.getAmount();
                    if (stillNeeded < 1) {
                        ingCountCopy.remove(blockName);
                        if (removeBlocks) {
                            iv.setAmount(iv.getAmount() - count);
                            containerInventory.setItem(ik, iv);
                        }
                    } else {
                        ingCountCopy.put(blockName, count);
                        if (removeBlocks) {
                            containerInventory.setItem(ik, null);
                        }
                    }
                }
            });
            bsm.setBlockState(container);
            v.setItemMeta(bsm);
            inventory.setItem(k, v);
            if (ingCountCopy.isEmpty()) {
                return;
            }
        });

        return ingCountCopy;
    }
}
