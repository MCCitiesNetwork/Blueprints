package io.github.bl3rune.blueprints.items;

import static io.github.bl3rune.blueprints.Blueprints.logger;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.utils.EncodingUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BlueprintItem extends ItemStack {

    public static final Material LOCKED_MATERIAL = Material.WRITTEN_BOOK;
    public static final Material UNLOCKED_MATERIAL = Material.WRITABLE_BOOK;
    public static final String BLU3PRINT_PREFIX = LegacyComponentSerializer.legacySection()
            .serialize(Component.text("Blueprint", NamedTextColor.BLUE));
    private static final String LEGACY_PREFIX = LegacyComponentSerializer.legacySection()
            .serialize(Component.text("Blu3print", NamedTextColor.BLUE));

    private static boolean displayNameMatchesPrefix(String displayName) {
        return displayName != null
                && (displayName.startsWith(BLU3PRINT_PREFIX) || displayName.startsWith(LEGACY_PREFIX));
    }

    public static BlueprintItem getBlankBlu3print() {
        BlueprintItem blu3print = new BlueprintItem(UNLOCKED_MATERIAL);
        ItemMeta meta = blu3print.getItemMeta();
        meta.setDisplayName(BLU3PRINT_PREFIX + " Writer");
        meta.setLore(Arrays.asList("Used for composing Blueprints"));
        blu3print.setItemMeta(meta);
        return blu3print;
    }

    public static BlueprintItem getFinishedBlu3print(String uuid, String author, String name, boolean isImported) {
        List<String> lore = Arrays.asList(author, uuid);
        BlueprintItem blu3print = new BlueprintItem(LOCKED_MATERIAL);
        ItemMeta meta = blu3print.getItemMeta();
        name = displayNameMatchesPrefix(name) ? EncodingUtils.modifierSplit(name)[1].trim() : name;
        meta.setDisplayName(BLU3PRINT_PREFIX + " : " + name);
        meta.setLore(lore);
        blu3print.setItemMeta(meta);
        return blu3print;
    }

    public static String extractCacheKeyFromBlu3print(ItemStack blu3print)  {
        ItemMeta meta = blu3print.getItemMeta();
        if (meta == null) {
            logger().warning("Blueprint item has no meta");
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) {
            logger().warning("Blueprint item has invalid lore");
            return null;
        }
        String key = lore.get(1);
        try {
            UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            logger().warning("Blueprint item has invalid uuid: " + key);
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
                    && item.hasItemMeta() && displayNameMatchesPrefix(item.getItemMeta().getDisplayName());
        } else if (blank.booleanValue()) {
            return item != null && item.getType().equals(UNLOCKED_MATERIAL) && item.hasItemMeta()
                    && displayNameMatchesPrefix(item.getItemMeta().getDisplayName());
        } else {
            return item != null && item.getType().equals(LOCKED_MATERIAL) && item.hasItemMeta()
                    && displayNameMatchesPrefix(item.getItemMeta().getDisplayName());
        }
    }

    public BlueprintItem() {
    }

    public BlueprintItem(Material material) {
        super(material);
    }

}
