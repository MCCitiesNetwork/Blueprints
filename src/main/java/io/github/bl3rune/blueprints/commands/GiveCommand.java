package io.github.bl3rune.blueprints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.items.BlueprintItem;

public class GiveCommand implements CommandExecutor {

    private final Blueprints instance;

    public GiveCommand(Blueprints instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(BlueprintItem.getBlankBlu3print());
            } else {
                return false;
            }
        } else {
            String playerName = args[0];
            if (playerName == null) {
                return false;
            }
            Player player = instance.getServer().getPlayerExact(playerName);
            if (player == null) {
                sender.sendMessage("Not a valid player name");
            } else {
                player.getInventory().addItem(BlueprintItem.getBlankBlu3print());
            }
        }
        return true;
    }

}
