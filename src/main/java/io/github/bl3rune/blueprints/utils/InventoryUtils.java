package io.github.bl3rune.blueprints.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.items.BlueprintItem;

public class InventoryUtils {

    public static boolean itemIsBlank(ItemStack item) {
        return item == null || item.getType().name().equals(Material.AIR.name());
    }

    // **** **** **** Blu3print methods **** **** ****

    public static ItemStack getHeldBlu3print(Player player, Boolean blank) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return BlueprintItem.isBlu3print(item, blank) ? item : null;
    }

    public static int getBlu3printInventoryPosition(Player player, Boolean blank) {
        ItemStack[] inventory = player.getInventory().getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (itemIsBlank(item)) continue;
            if (BlueprintItem.isBlu3print(item, blank)) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack getBlu3printInInventory(Player player, Boolean blank) {
        int pos = getBlu3printInventoryPosition(player, blank);
        return pos == -1 ? null : player.getInventory().getItem(pos);
    }

    public static boolean removeBlu3printFromInventory(Player player, Boolean blank) {
        int pos = getBlu3printInventoryPosition(player, blank);
        if (pos == -1) {
            return false;
        }
        ItemStack item = player.getInventory().getItem(pos);
        if (item.getAmount() == 1) {
            item = null;
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        player.getInventory().setItem(pos, item);
        return true;
    }

}
