package io.github.bl3rune.blueprints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.enums.Rotation;

public class RotateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = CommandSupport.requireHeldBlueprint(sender, player,
                    "You must be holding a blu3print to rotate it.");
            if (item == null) {
                return true;
            }

            Blu3printData data = CommandSupport.lookupCached(player, item);
            ManipulatablePosition pos = data.getPosition();

            Rotation rotation = pos.getRotation().getNextRotation();
            if (args.length > 0) {
                try {
                    rotation = Rotation.valueOf(args[0]);
                } catch (Exception e) {
                    sender.sendMessage("Invalid rotation argument, try one of the following: TOP/LEFT/RIGHT/BOTTOM");
                    return true;
                }
            }
            String newEncoding = data.updateEncodingWithRotation(rotation);
            if (newEncoding == null) {
                sender.sendMessage("Failed to update orientation");
                return true;
            }
            CommandSupport.applyEncodingUpdate(player, item, newEncoding);
        }
        return true;
    }
}
