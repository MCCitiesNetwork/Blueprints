package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.enums.Turn;

public class TurnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = CommandSupport.requireHeldBlueprint(sender, player,
                    "You must be holding a blueprint to turn what side faces you");
            if (item == null) {
                return true;
            }

            BlueprintData data = CommandSupport.lookupCached(player, item);
            Turn turn = null;
            if (args.length > 0) {
                try {
                    turn = Turn.valueOf(args[0]);
                } catch (Exception e) {
                    sender.sendMessage("Invalid turning argument, try one of the following: "
                            + Arrays.toString(Turn.values()));
                    return true;
                }
            }
            String newEncoding = data.updateEncodingWithTurn(turn);
            if (newEncoding == null) {
                sender.sendMessage("Failed to update orientation");
                return true;
            }
            CommandSupport.applyEncodingUpdate(player, item, newEncoding);
        }
        return true;
    }
}
