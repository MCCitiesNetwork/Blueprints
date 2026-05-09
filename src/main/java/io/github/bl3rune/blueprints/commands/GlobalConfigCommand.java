package io.github.bl3rune.blueprints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.GConfig;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

public class GlobalConfigCommand implements CommandExecutor {

    private Blueprints plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GConfig config = null;
        if (plugin == null) {
            plugin = Blueprints.getInstance();
        }

        if (args.length == 0) {
            printCurrentConfig(sender);
            return true;
        }

        try {
            String c = args[0];
            config = GConfig.valueOf(c.toUpperCase());
        } finally {
            if (config == null) {
                StringBuilder sb = new StringBuilder();
                for (GConfig cc : GConfig.values()) {
                    sb.append(cc.name()).append(" ");
                }
                sender.sendMessage(ChatColor.RED + "Not valid subcommand try : " + sb.toString());
                return true;
            }
        }
        try {
            switch (config) {
                case MAX_SCALE:
                case MAX_SIZE:
                case MAX_OVERALL_SIZE:
                case HOLOGRAM_TTL:
                case COOLDOWN:
                case UPDATE_CHECK_INTERVAL:
                    plugin.getConfig().set(config.getConfigPath(), Integer.parseInt(args[1]));
                    break;
                case ALIGNMENT:
                    plugin.getConfig().set(config.getConfigPath(), Alignment.valueOf(args[1]).name());
                    break;
                case UPDATE_LEVEL:
                    plugin.getConfig().set(config.getConfigPath(), SemanticLevel.valueOf(args[1]).name());
                    break;
                case RELATIVE:
                case FREE_PLACEMENT_MESSAGE:
                case FORCED_PLACEMENT_MESSAGE:
                case DISCOUNT_PLACEMENT_MESSAGE:
                case UPDATE_AVAILABLE_MESSAGE:
                case COOLDOWN_MESSAGE:
                case VERBOSE_LOGGING:
                case IMPORTED_BLU3PRINTS_LOGGING:
                    plugin.getConfig().set(config.getConfigPath(), Boolean.parseBoolean(args[1]));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage("Failed to set value " + config.name());
            return false;
        }
        GlobalConfig.refreshConfiguration();

        return true;
    }

    private void printCurrentConfig(CommandSender sender) {
        sender.sendMessage("Current Configurations:");
        for (GConfig config : GConfig.values()) {
            sender.sendMessage(config.getConfigPath() + ": " + config.getCurrentValue());
        }
    }

}
