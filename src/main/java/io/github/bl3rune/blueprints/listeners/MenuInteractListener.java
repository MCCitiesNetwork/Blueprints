package io.github.bl3rune.blueprints.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import io.github.bl3rune.blueprints.commands.BlueprintCommand;
import io.github.bl3rune.blueprints.enums.CommandType;
import io.github.bl3rune.blueprints.enums.MenuItems;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MenuInteractListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().title().equals(BlueprintCommand.BLU3PRINT_MENU_TITLE)) {
            event.setCancelled(true);

            if (event.isRightClick())
                return;

            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) {
                return;
            }
            String itemName = PlainTextComponentSerializer.plainText().serialize(
                    LegacyComponentSerializer.legacySection().deserialize(
                            event.getCurrentItem().getItemMeta().getDisplayName()));
            MenuItems menuItem = MenuItems.getMenuItem(itemName);
            if (menuItem == null) return;
            switch (menuItem) {
                case DUPLICATE:
                    player.performCommand(CommandType.DUPLICATE.getFullCommandName());
                    break;
                case ROTATE:
                    player.performCommand(CommandType.ROTATE.getFullCommandName());
                    break;
                case EXPORT:
                    player.performCommand(CommandType.EXPORT.getFullCommandName());
                    break;
                case SCALE:
                    player.performCommand(CommandType.SCALE.getFullCommandName());
                    break;
                case FACE:
                    player.performCommand(CommandType.FACE.getFullCommandName());
                    break;
                case TURN:
                    player.performCommand(CommandType.TURN.getFullCommandName());
                    break;
                case GIVE:
                    player.performCommand(CommandType.GIVE.getFullCommandName());
                    break;
                case HELP:
                    player.performCommand(CommandType.HELP.getFullCommandName());
                    break;
                case EXIT:
                    player.closeInventory();
                    break;
                default:
                    break;
            }
        }
    }

}
