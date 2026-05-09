package io.github.bl3rune.blueprints.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.enums.Config;

public class PlayerConfigCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Config config = null;
            try {
                String c = args[0];
                config = Config.valueOf(c.toUpperCase());
            } finally {
                if (config == null || !config.isPlayerLevelConfig()) {
                    StringBuilder sb = new StringBuilder();
                    for (Config cc : Config.values()) {
                        if (cc.isPlayerLevelConfig()) {
                            sb.append(cc.name()).append(" ");
                        }
                    }
                    player.sendMessage(ChatColor.RED + "Not valid subcommand try : " + sb.toString());
                    return true;
                }
            }

            String playerUUID = player.getUniqueId().toString();
            PlayerConfig pc = Blueprints.getPlayerConfig(playerUUID);
            if (pc == null) {
                pc = new PlayerConfig();
            }
            switch (config) {
                case CLEAR:
                    Blueprints.setPlayerConfig(playerUUID, null);
                    player.sendMessage(ChatColor.GREEN + "Player config reset!");
                    return true;
                case IGNORE_MATERIAL:
                case ALLOW_MATERIAL:
                    pc = modifyMaterialIgnoreList(pc, args, config, player);
                    break;
                default:
                    break;
            }
            if (pc != null) {
                Blueprints.setPlayerConfig(playerUUID, pc);
            }
        }
        return true;
    }

    private PlayerConfig modifyMaterialIgnoreList(PlayerConfig pc, String[] args, Config config,
            Player player) {
        if (args.length < 2) {
            player.sendMessage("Usage: /blu3print.player-config IGNORE_MATERIALS [material]");
            return pc;
        }
        Material material; 
        try {
            material = Material.matchMaterial(args[1]);
            if (material == null) {
                player.sendMessage(ChatColor.RED + "Invalid material : " + args[1]);
                return pc;
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid material : " + args[1]);
            return pc;
        }
        List<String> ignoredMaterials = pc.getIgnoredMaterials();
        if (config == Config.IGNORE_MATERIAL) {
            if (GlobalConfig.isVerboseLogging()) {
                player.sendMessage(ChatColor.GREEN + "Added " + material.name() + " to ignore list");
            }
            ignoredMaterials.add(material.name().toUpperCase());
        } else {
            if (GlobalConfig.isVerboseLogging()) {
                player.sendMessage(ChatColor.RED + "Removed " + material.name() + " froom ignore list");
            }
            ignoredMaterials.removeIf(m -> m.equalsIgnoreCase(material.name()));
        }
        pc.setIgnoredMaterials(ignoredMaterials);
        return pc;
    }

}
