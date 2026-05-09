package io.github.bl3rune.blueprints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.items.BlueprintItem;
import io.github.bl3rune.blueprints.utils.InventoryUtils;

public class NameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 1) {
                sender.sendMessage("You must provide a name for the blu3print");
                return false;
            }
            ItemStack blu3print = InventoryUtils.getHeldBlu3print(player, false);
            if (blu3print == null) {
                sender.sendMessage("You must be holding a blu3print to name/rename it.");
                return false;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(BlueprintItem.BLU3PRINT_PREFIX + " :");
            for (String arg : args) {
                sb.append(" ");
                sb.append(arg);
            }
            ItemMeta itemMeta = blu3print.getItemMeta();
            itemMeta.setDisplayName(sb.toString());
            blu3print.setItemMeta(itemMeta);
            player.getInventory().setItemInMainHand(blu3print);
            player.sendMessage( "Blu3print name changed to: " + sb.toString());
        }
        return true;
    }

}
