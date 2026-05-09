package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.enums.MenuItems;

public class Blu3printCommand implements CommandExecutor {

    private Inventory inventory;

    public static final String BLU3PRINT_MENU_STRING = ChatColor.BLUE + "Blu3print Menu";
     
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if  (inventory == null) {
                inventory = Bukkit.createInventory(player, 18, BLU3PRINT_MENU_STRING);

                int index = 0;
                for (MenuItems item : MenuItems.values()) {
                    ItemStack menuItem = new ItemStack(item.getMaterial(), item.getAmount());
                    setItemMeta(menuItem, item.getFormattedName(), item.getFormattedDescription());
                    if (item.equals(MenuItems.EXIT)) {
                        inventory.setItem(8, menuItem);
                        continue;
                    } else {
                        inventory.setItem(index, menuItem);
                    }
                    index++;
                    if (index == 8) index++;
                }
                
            }

            player.openInventory(inventory);
            return true;
        }

        return true;
    }

    private ItemMeta setItemMeta(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return meta;
    }

}
