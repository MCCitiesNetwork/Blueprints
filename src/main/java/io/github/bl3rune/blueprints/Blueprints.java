package io.github.bl3rune.blueprints;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bl3rune.blueprints.config.PlayerBlueprintConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.core.PluginBootstrap;
import io.github.bl3rune.blueprints.core.ServiceRegistry;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.services.BlueprintCacheService;
import io.github.bl3rune.blueprints.services.PlayerSessionService;

/**
 * Main plugin class. After Phase 3 of the architecture overhaul this class
 * is reduced to Bukkit lifecycle hooks plus a thin facade that delegates
 * to services held in the {@link ServiceRegistry}. The static accessors
 * remain only as a compile shim until Phase 5 rewires commands and
 * listeners to take services through their constructors.
 *
 * @author bl3rune
 */
public final class Blueprints extends JavaPlugin {

    private static Blueprints instance;
    private static final List<String> updateMessages = new ArrayList<>();

    private PluginBootstrap bootstrap;

    public static Blueprints getInstance() {
        return instance;
    }

    public static List<String> getUpdateMessages() {
        return updateMessages;
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    public static PlayerConfig getPlayerConfig(String playerUUID) {
        return playerSessions().getPlayerConfig(playerUUID);
    }

    public static void setPlayerConfig(String playerUUID, PlayerConfig config) {
        playerSessions().setPlayerConfig(playerUUID, config);
    }

    public static PlayerBlueprintConfig getPlayerBlueprintConfig(String playerUUID) {
        return playerSessions().getPlayerBlueprintConfig(playerUUID);
    }

    public static void setPlayerBlueprintConfig(String playerUUID, PlayerBlueprintConfig config) {
        playerSessions().setPlayerBlueprintConfig(playerUUID, config);
    }

    private static PlayerSessionService playerSessions() {
        return instance.bootstrap.getRegistry().get(PlayerSessionService.class);
    }

    @Override
    public void onEnable() {
        instance = this;
        bootstrap = new PluginBootstrap(this);
        bootstrap.start();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }

    public ServiceRegistry getServiceRegistry() {
        return bootstrap == null ? null : bootstrap.getRegistry();
    }

    private BlueprintCacheService cache() {
        return bootstrap.getRegistry().get(BlueprintCacheService.class);
    }

    public BlueprintData getBlueprintFromCache(ItemStack blu3print, Player player) {
        return cache().get(blu3print, player);
    }

    public BlueprintData getBlueprintFromCache(String key) {
        return cache().get(key);
    }

    public String getKeyFromEncoding(String encoded) {
        return cache().keyFromEncoding(encoded);
    }

    public void saveOrUpdateCachedBlu3print(String key, BlueprintData data) {
        cache().saveOrUpdate(key, data);
    }
}
