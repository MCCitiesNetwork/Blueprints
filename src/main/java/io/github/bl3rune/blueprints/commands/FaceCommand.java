package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.enums.Orientation;

public class FaceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = CommandSupport.requireHeldBlueprint(sender, player,
                    "You must be holding a blu3print to change what side faces you");
            if (item == null) {
                return true;
            }

            Blu3printData data = CommandSupport.lookupCached(player, item);
            Orientation orientation = data.getPosition().getOrientation().getNextOrientation();
            if (args.length > 0) {
                try {
                    orientation = Orientation.valueOf(args[0]);
                } catch (Exception e) {
                    sender.sendMessage("Invalid orientation argument, try one of the following: "
                            + Arrays.toString(Orientation.values()));
                    return true;
                }
            }
            String newEncoding = data.updateEncodingWithOrientation(orientation);
            if (newEncoding == null) {
                sender.sendMessage("Failed to update orientation");
                return true;
            }
            CommandSupport.applyEncodingUpdate(player, item, newEncoding);
        }
        return true;
    }
}
