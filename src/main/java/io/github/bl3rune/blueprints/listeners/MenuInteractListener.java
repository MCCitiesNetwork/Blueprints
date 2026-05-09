package io.github.bl3rune.blueprints.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import io.github.bl3rune.blueprints.enums.CommandType;
import io.github.bl3rune.blueprints.enums.MenuItems;

public class MenuInteractListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.BLUE + "Blu3print Menu")) {
            event.setCancelled(true);

            if (event.isRightClick())
                return;

            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) {
                return;
            }
            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
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
