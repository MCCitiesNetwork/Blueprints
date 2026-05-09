package io.github.bl3rune.blueprints.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.data.ImportedBlueprintData;
import io.github.bl3rune.blueprints.items.BlueprintItem;
import io.github.bl3rune.blueprints.utils.InventoryUtils;

/**
 * Shared command helpers introduced in Phase 5 to remove the
 * "get held blueprint -> resolve cached data -> apply update -> persist
 * new encoding -> swap held item" boilerplate that Face/Rotate/Turn/Scale
 * each duplicated. Behavior matches the prior inline implementations
 * exactly: same player messages, same fallthrough on null data, same UUID
 * fallback when the new encoding is not yet cached.
 */
public final class CommandSupport {

    private CommandSupport() {
    }

    public static ItemStack requireHeldBlueprint(CommandSender sender, Player player, String missingMessage) {
        ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
        if (item == null) {
            sender.sendMessage(missingMessage);
            return null;
        }
        return item;
    }

    public static BlueprintData lookupCached(Player player, ItemStack item) {
        BlueprintData data = Blueprints.getInstance().getBlueprintFromCache(item, player);
        if (data == null) {
            player.sendMessage("Blu3print data not found");
        }
        return data;
    }

    public static void applyEncodingUpdate(Player player, ItemStack item, String newEncoding) {
        Blueprints instance = Blueprints.getInstance();
        String key = instance.getKeyFromEncoding(newEncoding);
        if (key == null) {
            key = UUID.randomUUID().toString();
            instance.saveOrUpdateCachedBlu3print(key, new ImportedBlueprintData(player, newEncoding, key));
        }
        ItemMeta meta = item.getItemMeta();
        BlueprintItem newItem = BlueprintItem.getFinishedBlu3print(key,
                "modified by " + player.getDisplayName(), meta.getDisplayName(), false);
        player.getInventory().setItemInMainHand(newItem);
    }
}
