package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ImportedBlu3printData;
import io.github.bl3rune.blueprints.enums.Turn;
import io.github.bl3rune.blueprints.items.Blu3printItem;
import io.github.bl3rune.blueprints.utils.InventoryUtils;

public class TurnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (item == null) {
                sender.sendMessage("You must be holding a blu3print to turn what side faces you");
                return true;
            }

            Blu3printData data = Blueprints.getInstance().getBlu3printFrpmCache(item, player);
            if (data == null) {
                player.sendMessage("Blu3print data not found");
            }
            Turn turn = null;
            if (args.length > 0) {
                try {
                    turn = Turn.valueOf(args[0]);
                } catch(Exception e) {
                    sender.sendMessage("Invalid turning argument, try one of the following: " + Arrays.toString(Turn.values()));
                    return true;
                }
            }
            String newEncoding = data.updateEncodingWithTurn(turn);
            if (newEncoding == null) {
                sender.sendMessage("Failed to update orientation");
                return true;
            }
            String key = Blueprints.getInstance().getKeyFromEncoding(newEncoding);
            if (key == null) {
                key = UUID.randomUUID().toString();
                Blueprints.getInstance().saveOrUpdateCachedBlu3print(key, new ImportedBlu3printData(player, newEncoding, key));
            }
            ItemMeta meta = item.getItemMeta();
            Blu3printItem newItem = Blu3printItem.getFinishedBlu3print(key, "modified by " + player.getDisplayName(), meta.getDisplayName(), false);
            player.getInventory().setItemInMainHand(newItem);
        }
        return true;
    }

}
