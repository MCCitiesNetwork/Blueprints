package io.github.bl3rune.blueprints.services;

import java.util.HashMap;
import java.util.Map;

import io.github.bl3rune.blueprints.config.PlayerBlu3printConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;

/**
 * Holds the per-player session state (player config + per-blueprint config)
 * previously kept in static maps on the plugin main class. Both caches clear
 * on server restart, which matches prior behavior.
 */
public final class PlayerSessionService {

    private final Map<String, PlayerConfig> playerConfigs = new HashMap<>();
    private final Map<String, PlayerBlu3printConfig> playerBlueprintConfigs = new HashMap<>();

    public PlayerConfig getPlayerConfig(String playerUUID) {
        return playerConfigs.getOrDefault(playerUUID, null);
    }

    public void setPlayerConfig(String playerUUID, PlayerConfig config) {
        if (config == null) {
            playerConfigs.remove(playerUUID);
        } else {
            playerConfigs.put(playerUUID, config);
        }
    }

    public PlayerBlu3printConfig getPlayerBlueprintConfig(String playerUUID) {
        return playerBlueprintConfigs.getOrDefault(playerUUID, null);
    }

    public void setPlayerBlueprintConfig(String playerUUID, PlayerBlu3printConfig config) {
        if (config == null) {
            playerBlueprintConfigs.remove(playerUUID);
        } else {
            playerBlueprintConfigs.put(playerUUID, config);
        }
    }
}
