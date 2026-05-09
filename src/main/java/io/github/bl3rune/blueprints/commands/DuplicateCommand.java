package io.github.bl3rune.blueprints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.utils.InventoryUtils;

public class DuplicateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack blu3print = InventoryUtils.getHeldBlu3print(player, false);
            if (blu3print == null) {
                sender.sendMessage("You must be holding a blu3print to duplicate it.");
                return true;
            }
            sender.sendMessage("Duplicating blu3print...");
            blu3print.setAmount(blu3print.getAmount() + 1);
            player.getInventory().setItemInMainHand(blu3print);
        }

        return true;
    }

}
