package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.Blueprints;

public class HelpCommand implements CommandExecutor {
     
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                switch (args[0]) {
                    case "writer":
                        writerHelp(player);
                        break;
                    case "usage":
                        blu3printHelp(player);
                        break;
                    case "table":
                        tableHelp(player);
                        break;
                    case "commands":
                        commandsHelp(player);
                        break;
                    case "config":
                        configHelp(player);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Invalid command. Try \n" + wrapCommand("/blu3print.help writer/usage/table/commands/config"));
                        break;
                }
                return true;
            }
            player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Basics Help #####");
            player.sendMessage(ChatColor.WHITE + "First start by crafting a Blu3print Writer in the crafting table using these materials:");
            List<String> ingredients = Blueprints.getInstance().getConfig().getStringList("blu3print.recipe.ingredients");
            if (ingredients == null || ingredients.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "> [PAPER, LAPIS_LAZULI, FEATHER]");
            } else {
                player.sendMessage(ChatColor.GRAY + "> " + Arrays.toString(ingredients.toArray()));
            }
            player.sendMessage(ChatColor.GRAY + "For information on how to use a Blu3print Writer use the help command again like this: " + wrapCommand("/blu3print.help writer"));
            player.sendMessage(ChatColor.GRAY + "For information on how to use a finished Blu3print use the help command again like this: " + wrapCommand("/blu3print.help usage"));
            player.sendMessage(ChatColor.GRAY + "For information on how to use a Blu3print with the cartography table use the help command again like this: " + wrapCommand("/blu3print.help table"));
            player.sendMessage(ChatColor.GRAY + "For information on how to use Blu3print commands use the help command again like this: " + wrapCommand("/blu3print.help commands"));
            player.sendMessage(ChatColor.GRAY + "For information on how to use Blu3print config command use the help command again like this: " + wrapCommand("/blu3print.help config"));
        }

        return true;
    }

    private void writerHelp(Player player) {
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Writer Help #####");
        player.sendMessage(ChatColor.WHITE + "While holding a Blu3print Writer, you can interact by:");
        player.sendMessage(ChatColor.GRAY + "Using left click on a block to set the first position of the selection area (this clears the ignore list if there is one)");
        player.sendMessage(ChatColor.GRAY + "Using left click (while sneaking) on a block to ignore/unignore the block within a selection");
        player.sendMessage(ChatColor.GRAY + "Using right click on a block to set the second position of the selection area (this clears the ignore list if there is one)");
        player.sendMessage(ChatColor.GRAY + "Using right click (while sneaking) on a block to show the ignore block list within the current selection");
        player.sendMessage(ChatColor.GRAY + "Using right click on the air to set open the blu3print writer");
        player.sendMessage(ChatColor.WHITE + "With the blu3print writer open:");
        player.sendMessage(ChatColor.GRAY + "Click on sign and give the blu3print a name to complete it");
        player.sendMessage(ChatColor.GRAY + "Or instead of using area selection, enter a blu3print code on the pages of the book and then sign and complete");
    }

    private void blu3printHelp(Player player)  {
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Usage Help #####");
        player.sendMessage(ChatColor.WHITE + "While holding a completed Blu3print, you can interact by:");
        player.sendMessage(ChatColor.GRAY + "Using left click on a block to build the blu3print from that block");
        player.sendMessage(ChatColor.GRAY + "Using left click (while sneaking) on a block to build the blu3print from that block even if there are blocks in the way");
        player.sendMessage(ChatColor.GRAY + "Using right click on the air to explain the blu3print");
        player.sendMessage(ChatColor.GRAY + "Using right click on a block to place a holographic representaation of the blocks about to be placed");
        player.sendMessage(ChatColor.GRAY + "Using right click (while sneaking) on a block to force place on the same height as the block clicked (useful for bridges)");
    }

    private void tableHelp(Player player)  {
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Cartography Table Help #####");
        player.sendMessage(ChatColor.WHITE + "While holding a completed Blu3print, you can interact by:");
        player.sendMessage(ChatColor.GRAY + "Using right click on a cartography table to open the blu3print menu");
    }

    private void commandsHelp(Player player)   {
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Commands Help #####");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.help") + " for help with the plugin");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.give") + " or " + wrapCommand("/blu3print.help <player>") + " to give a Blu3print to a player");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.global-config") + " to change config for the plugin");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.player-config") + " to change player-level config for the plugin");
        player.sendMessage(ChatColor.WHITE + "While holding a completed Blu3print:");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print") + " to open the blu3print menu");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.name") + " to name the blu3print");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.export") + " to export the blu3print to chat");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.rotate") + " or " + wrapCommand("/blu3print <rotation>") + " to rotate the blu3print");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.face") + " or " + wrapCommand("/blu3print.face <side>") + " to change the side of the blu3print facing you");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.turn <turn>") + " to turn the side of the blu3print facing you");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.duplicate") + " to duplicate the blu3print");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.scale") + " to change the scale of the blu3print");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.config") + " to change cnfiguration for this blu3print");
        player.sendMessage(ChatColor.WHITE + "While holding a Blu3print Writer:");
        player.sendMessage(ChatColor.GRAY + wrapCommand("/blu3print.import <name> <encoding>") + " to import a blu3print from text");
    }

    private void configHelp(Player player) {
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "##### Blu3Print Config Command Help #####");
        player.sendMessage(ChatColor.YELLOW + "Use /blu3print.config to set Blu3print specific config");
        player.sendMessage(ChatColor.YELLOW + "Use /blu3print.player-config to set player specific config");
        player.sendMessage(ChatColor.YELLOW + "Use /blu3print.global-config to set Blu3print plugin config");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config") + " to set player blu3print config");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config CLEAR") + " to clear player blu3print config");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config HOLOGRAM_VIEW_XYZ 0-2 0 0-1") + " to set hologram for current blu3print to only show blocks in layers X 0,1,2 and Y 0 and Z 0,1");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config HOLOGRAM_VIEW_X 0,2-3,5-7") + " to set hologram for current blu3print to only show blocks in the X layers 0,2,3,5,6,7");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config HOLOGRAM_VIEW_Y 0,2-3,5-7") + " to set hologram for current blu3print to only show blocks in the Y layers 0,2,3,5,6,7");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config HOLOGRAM_VIEW_Z 0,2-3,5-7") + " to set hologram for current blu3print to only show blocks in the Z layers 0,2,3,5,6,7");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config IGNORE_MATERIAL <material>") + " to ignore material when capturing/placing/previewing blu3prints");
        player.sendMessage(ChatColor.GRAY + "Type " + wrapCommand("/blu3print.config ALLOW_MATERIAL <material>") + "  to remove material from ignore list");

    }

    private String wrapCommand(String command) {
        return ChatColor.YELLOW + command + ChatColor.GRAY;
    }

}
