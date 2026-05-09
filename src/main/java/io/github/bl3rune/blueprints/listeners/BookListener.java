package io.github.bl3rune.blueprints.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.data.CapturedBlueprintData;
import io.github.bl3rune.blueprints.data.ImportedBlueprintData;
import io.github.bl3rune.blueprints.items.BlueprintItem;
import io.github.bl3rune.blueprints.utils.InventoryUtils;

public class BookListener implements Listener {

    private final Blueprints instance;

    public BookListener(Blueprints instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onBookFinished(PlayerEditBookEvent event) {

        Player player = event.getPlayer();
        if (player == null || !event.isSigning()) {
            return;
        }

        ItemStack book = InventoryUtils.getHeldBlu3print(player, true);
        if (book == null)  {
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ItemMeta itemMeta = book.getItemMeta();
        BookMeta bookMeta = event.getNewBookMeta();

        BlueprintItem finishedBook = null;
        BlueprintData blu3printData = null;
        event.setCancelled(true);

        if (bookMeta.hasPages() && !bookMeta.getPages().isEmpty() && bookMeta.getPages().stream().anyMatch(p -> !p.isEmpty())) {
            List<String> pages = bookMeta.getPages();
            StringBuilder builder = new StringBuilder();
            for (String page : pages) {
                builder.append(page);
            }
            String encodedString = builder.toString().replaceAll("\\s+", "");
            String cacheKey = instance.getKeyFromEncoding(encodedString);
            if (cacheKey != null) {
                uuid = cacheKey;
            }
            finishedBook = BlueprintItem.getFinishedBlu3print(uuid, "imported by " + player.getDisplayName(), bookMeta.getTitle(), true);
            blu3printData = new ImportedBlueprintData(player, encodedString, uuid);
            if (blu3printData.getPosition() == null) return;
            bookMeta.setPages(new ArrayList<>());
            book.setItemMeta(bookMeta);
        } else {
            String playerUuid = player.getUniqueId().toString();
            String pos1 = extractLocation(itemMeta,  "location1-" + playerUuid);
            String pos2 = extractLocation(itemMeta,  "location2-" + playerUuid);
            finishedBook = BlueprintItem.getFinishedBlu3print(uuid, "created by " + player.getDisplayName(), bookMeta.getTitle(), false);
            blu3printData = new CapturedBlueprintData(player, pos1, pos2, uuid);
            if (blu3printData.getPosition() == null) return;
        }
        instance.saveOrUpdateCachedBlu3print(uuid, blu3printData);
        finishedBook.setAmount(1);
        player.getInventory().addItem(finishedBook);
        player.sendMessage("You have finished your blu3print!");
        instance.getLogger().info("Finished blu3print: " + blu3printData.getEncodedString());
    }

    private String extractLocation(ItemMeta meta, String key) {
        NamespacedKey nsKey = new NamespacedKey(instance, key);
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.getOrDefault(nsKey, PersistentDataType.STRING, null);

    }

}
