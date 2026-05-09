package io.github.bl3rune.blueprints.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.Blueprints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
                        blueprintHelp(player);
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
                        player.sendMessage(Component.text("Invalid command. Try \n", NamedTextColor.RED)
                                .append(command0("/blueprint.help writer/usage/table/commands/config")));
                        break;
                }
                return true;
            }
            player.sendMessage(header("##### Blueprint Basics Help #####"));
            player.sendMessage(white("First start by crafting a Blueprint Writer in the crafting table using these materials:"));
            List<String> ingredients = Blueprints.getInstance().getConfig().getStringList("blu3print.recipe.ingredients");
            if (ingredients == null || ingredients.isEmpty()) {
                player.sendMessage(gray("> [PAPER, LAPIS_LAZULI, FEATHER]"));
            } else {
                player.sendMessage(gray("> " + Arrays.toString(ingredients.toArray())));
            }
            player.sendMessage(gray(t("For information on how to use a Blueprint Writer use the help command again like this: "),
                    command0("/blueprint.help writer")));
            player.sendMessage(gray(t("For information on how to use a finished Blueprint use the help command again like this: "),
                    command0("/blueprint.help usage")));
            player.sendMessage(gray(t("For information on how to use a Blueprint with the cartography table use the help command again like this: "),
                    command0("/blueprint.help table")));
            player.sendMessage(gray(t("For information on how to use Blueprint commands use the help command again like this: "),
                    command0("/blueprint.help commands")));
            player.sendMessage(gray(t("For information on how to use Blueprint config command use the help command again like this: "),
                    command0("/blueprint.help config")));
        }

        return true;
    }

    private void writerHelp(Player player) {
        player.sendMessage(header("##### Blueprint Writer Help #####"));
        player.sendMessage(white("While holding a Blueprint Writer, you can interact by:"));
        player.sendMessage(gray("Using left click on a block to set the first position of the selection area (this clears the ignore list if there is one)"));
        player.sendMessage(gray("Using left click (while sneaking) on a block to ignore/unignore the block within a selection"));
        player.sendMessage(gray("Using right click on a block to set the second position of the selection area (this clears the ignore list if there is one)"));
        player.sendMessage(gray("Using right click (while sneaking) on a block to show the ignore block list within the current selection"));
        player.sendMessage(gray("Using right click on the air to set open the blueprint writer"));
        player.sendMessage(white("With the blueprint writer open:"));
        player.sendMessage(gray("Click on sign and give the blueprint a name to complete it"));
        player.sendMessage(gray("Or instead of using area selection, enter a blueprint code on the pages of the book and then sign and complete"));
    }

    private void blueprintHelp(Player player)  {
        player.sendMessage(header("##### Blueprint Usage Help #####"));
        player.sendMessage(white("While holding a completed Blueprint, you can interact by:"));
        player.sendMessage(gray("Using left click on a block to build the blueprint from that block"));
        player.sendMessage(gray("Using left click (while sneaking) on a block to build the blueprint from that block even if there are blocks in the way"));
        player.sendMessage(gray("Using right click on the air to explain the blueprint"));
        player.sendMessage(gray("Using right click on a block to place a holographic representaation of the blocks about to be placed"));
        player.sendMessage(gray("Using right click (while sneaking) on a block to force place on the same height as the block clicked (useful for bridges)"));
    }

    private void tableHelp(Player player)  {
        player.sendMessage(header("##### Blueprint Cartography Table Help #####"));
        player.sendMessage(white("While holding a completed Blueprint, you can interact by:"));
        player.sendMessage(gray("Using right click on a cartography table to open the blueprint menu"));
    }

    private void commandsHelp(Player player)   {
        player.sendMessage(header("##### Blueprint Commands Help #####"));
        player.sendMessage(gray(t("Type "), command0("/blueprint.help"), t(" for help with the plugin")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.give"), t(" or "), command0("/blueprint.help <player>"), t(" to give a Blueprint to a player")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.global-config"), t(" to change config for the plugin")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.player-config"), t(" to change player-level config for the plugin")));
        player.sendMessage(white("While holding a completed Blueprint:"));
        player.sendMessage(gray(command0("/blueprint"), t(" to open the blueprint menu")));
        player.sendMessage(gray(command0("/blueprint.name"), t(" to name the blueprint")));
        player.sendMessage(gray(command0("/blueprint.export"), t(" to export the blueprint to chat")));
        player.sendMessage(gray(command0("/blueprint.rotate"), t(" or "), command0("/blueprint <rotation>"), t(" to rotate the blueprint")));
        player.sendMessage(gray(command0("/blueprint.face"), t(" or "), command0("/blueprint.face <side>"), t(" to change the side of the blueprint facing you")));
        player.sendMessage(gray(command0("/blueprint.turn <turn>"), t(" to turn the side of the blueprint facing you")));
        player.sendMessage(gray(command0("/blueprint.duplicate"), t(" to duplicate the blueprint")));
        player.sendMessage(gray(command0("/blueprint.scale"), t(" to change the scale of the blueprint")));
        player.sendMessage(gray(command0("/blueprint.config"), t(" to change cnfiguration for this blueprint")));
        player.sendMessage(white("While holding a Blueprint Writer:"));
        player.sendMessage(gray(command0("/blueprint.import <name> <encoding>"), t(" to import a blueprint from text")));
    }

    private void configHelp(Player player) {
        player.sendMessage(header("##### Blueprint Config Command Help #####"));
        player.sendMessage(yellow("Use /blueprint.config to set Blueprint specific config"));
        player.sendMessage(yellow("Use /blueprint.player-config to set player specific config"));
        player.sendMessage(yellow("Use /blueprint.global-config to set Blueprint plugin config"));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config"), t(" to set player blueprint config")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config CLEAR"), t(" to clear player blueprint config")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config HOLOGRAM_VIEW_XYZ 0-2 0 0-1"), t(" to set hologram for current blueprint to only show blocks in layers X 0,1,2 and Y 0 and Z 0,1")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config HOLOGRAM_VIEW_X 0,2-3,5-7"), t(" to set hologram for current blueprint to only show blocks in the X layers 0,2,3,5,6,7")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config HOLOGRAM_VIEW_Y 0,2-3,5-7"), t(" to set hologram for current blueprint to only show blocks in the Y layers 0,2,3,5,6,7")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config HOLOGRAM_VIEW_Z 0,2-3,5-7"), t(" to set hologram for current blueprint to only show blocks in the Z layers 0,2,3,5,6,7")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config IGNORE_MATERIAL <material>"), t(" to ignore material when capturing/placing/previewing blueprints")));
        player.sendMessage(gray(t("Type "), command0("/blueprint.config ALLOW_MATERIAL <material>"), t("  to remove material from ignore list")));
    }

    private static Component header(String text) {
        return Component.text(text, NamedTextColor.BLUE, TextDecoration.BOLD);
    }

    private static Component white(String text) {
        return Component.text(text, NamedTextColor.WHITE);
    }

    private static Component yellow(String text) {
        return Component.text(text, NamedTextColor.YELLOW);
    }

    private static Component command0(String command) {
        return Component.text(command, NamedTextColor.YELLOW);
    }

    /**
     * Build a gray-styled parent with the given children appended. Children
     * with unset color/decorations inherit the parent's gray via Adventure's
     * style inheritance; styled children (e.g. {@link #command0}) keep theirs.
     */
    private static Component gray(String text) {
        return Component.text(text, NamedTextColor.GRAY);
    }

    private static Component gray(Component... parts) {
        Component parent = Component.text().color(NamedTextColor.GRAY).build();
        for (Component part : parts) {
            parent = parent.append(part);
        }
        return parent;
    }

    private static Component t(String text) {
        return Component.text(text);
    }
}
