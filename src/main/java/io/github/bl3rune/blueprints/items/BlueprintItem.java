package io.github.bl3rune.blueprints.items;

import static io.github.bl3rune.blueprints.Blueprints.logger;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.utils.EncodingUtils;

public class BlueprintItem extends ItemStack {

    public static final Material LOCKED_MATERIAL = Material.WRITTEN_BOOK;
    public static final Material UNLOCKED_MATERIAL = Material.WRITABLE_BOOK;
    public static final String BLU3PRINT_PREFIX = ChatColor.BLUE + "Blu3print";

    public static BlueprintItem getBlankBlu3print() {
        BlueprintItem blu3print = new BlueprintItem(UNLOCKED_MATERIAL);
        ItemMeta meta = blu3print.getItemMeta();
        meta.setDisplayName(BLU3PRINT_PREFIX + " Writer");
        meta.setLore(Arrays.asList("Used for composing Blu3prints"));
        blu3print.setItemMeta(meta);
        return blu3print;
    }

    public static BlueprintItem getFinishedBlu3print(String uuid, String author, String name, boolean isImported) {
        List<String> lore = Arrays.asList(author, uuid);
        BlueprintItem blu3print = new BlueprintItem(LOCKED_MATERIAL);
        ItemMeta meta = blu3print.getItemMeta();
        name = name.startsWith(BLU3PRINT_PREFIX) ? EncodingUtils.modifierSplit(name)[1].trim() : name;
        meta.setDisplayName(BLU3PRINT_PREFIX + " : " + name);
        meta.setLore(lore);
        blu3print.setItemMeta(meta);
        return blu3print;
    }

    public static String extractCacheKeyFromBlu3print(ItemStack blu3print)  {
        ItemMeta meta = blu3print.getItemMeta();
        if (meta == null) {
            logger().warning("Blu3print item has no meta");
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) {
            logger().warning("Blu3print item has invalid lore");
            return null;
        }
        String key = lore.get(1);
        try {
            UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            logger().warning("Blu3print item has invalid uuid: " + key);
            return null;
        }
        return key;
     
    }

    /**
     * Method that checks for
     * 
     * @param item  item to check
     * @param blank if null checks for both types if not checks for blank if true
     *              and completed if false
     * @return
     */
    public static boolean isBlu3print(ItemStack item, Boolean blank) {
        if (blank == null) {
            return item != null && (item.getType().equals(UNLOCKED_MATERIAL) || item.getType().equals(LOCKED_MATERIAL))
                    && item.hasItemMeta() && item.getItemMeta().getDisplayName().startsWith(BLU3PRINT_PREFIX);
        } else if (blank.booleanValue()) {
            return item != null && item.getType().equals(UNLOCKED_MATERIAL) && item.hasItemMeta()
                    && item.getItemMeta().getDisplayName().startsWith(BLU3PRINT_PREFIX);
        } else {
            return item != null && item.getType().equals(LOCKED_MATERIAL) && item.hasItemMeta()
                    && item.getItemMeta().getDisplayName().startsWith(BLU3PRINT_PREFIX);
        }
    }

    public BlueprintItem() {
    }

    public BlueprintItem(Material material) {
        super(material);
    }

}
