package io.github.bl3rune.blueprints.services.domain;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerBlu3printConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.services.PlayerSessionService;

/**
 * Single source of truth for the ignore-material logic that was previously
 * duplicated between {@code Blu3printData} and {@code Hologram}. Owns the
 * lazily-loaded global ignore list and combines player-/blueprint-specific
 * lists with the global list when checking material ignorability.
 */
public final class MaterialIgnoreResolver {

    private final PlayerSessionService sessions;
    private List<String> globalIgnoreList = new ArrayList<>();

    public MaterialIgnoreResolver(PlayerSessionService sessions) {
        this.sessions = sessions;
    }

    public List<String> resolvePlayerIgnoreList(Player player, String blu3printUUID) {
        ensureGlobalLoaded();
        List<String> ignoreList = new ArrayList<>();
        if (player == null) {
            return ignoreList;
        }
        String playerUUID = player.getUniqueId().toString();
        PlayerBlu3printConfig pbc = sessions.getPlayerBlueprintConfig(playerUUID);
        if (pbc != null) {
            if (pbc.uuidMatches(blu3printUUID)) {
                ignoreList.addAll(pbc.getIgnoredMaterials());
            } else {
                player.sendMessage(ChatColor.RED + "Cleared blu3print config as using different blu3print!");
                sessions.setPlayerBlueprintConfig(playerUUID, null);
            }
        }
        PlayerConfig pc = sessions.getPlayerConfig(playerUUID);
        if (pc != null) {
            ignoreList.addAll(pc.getIgnoredMaterials());
        }
        return ignoreList;
    }

    public boolean isIgnorable(Material material, List<String> playerIgnoreList) {
        ensureGlobalLoaded();
        if (material == null || material.isAir()) {
            return true;
        }
        String upper = material.name().toUpperCase();
        return (playerIgnoreList != null && playerIgnoreList.contains(upper))
                || globalIgnoreList.contains(upper);
    }

    private void ensureGlobalLoaded() {
        if (globalIgnoreList.isEmpty()) {
            globalIgnoreList = GlobalConfig.getIgnoredMaterials();
        }
    }
}
