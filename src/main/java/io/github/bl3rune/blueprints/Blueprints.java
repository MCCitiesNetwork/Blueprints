package io.github.bl3rune.blueprints;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerBlu3printConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.core.PluginBootstrap;
import io.github.bl3rune.blueprints.core.ServiceRegistry;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ImportedBlu3printData;
import io.github.bl3rune.blueprints.items.Blu3printItem;

/**
 * Main plugin class. After Phase 2 of the architecture overhaul this class
 * is reduced to Bukkit lifecycle hooks and a small set of legacy accessors
 * that later phases (3+) will replace with injected services. All startup
 * and shutdown orchestration lives in {@link PluginBootstrap}.
 *
 * @author bl3rune
 */
public final class Blueprints extends JavaPlugin {

    private static Blueprints instance;
    private static List<String> updateMessages = new ArrayList<>();
    private static Map<String, PlayerConfig> playerConfig = new HashMap<>();
    private static Map<String, PlayerBlu3printConfig> playerBlu3printConfig = new HashMap<>();

    private PluginBootstrap bootstrap;
    private final HashMap<String, Blu3printData> cachedBlueprints = new HashMap<>();
    private final Gson gson = new Gson();

    public static Blueprints getInstance() {
        return instance;
    }

    public static List<String> getUpdateMessages() {
        return updateMessages;
    }

    public static PlayerConfig getPlayerConfig(String playerUUID) {
        return playerConfig.getOrDefault(playerUUID, null);
    }

    public static void setPlayerConfig(String playerUUID, PlayerConfig ppbc) {
        if (ppbc == null) {
            playerConfig.remove(playerUUID);
        } else {
            playerConfig.put(playerUUID, ppbc);
        }
    }

    public static PlayerBlu3printConfig getPlayerBlu3printConfig(String playerUUID) {
        return playerBlu3printConfig.getOrDefault(playerUUID, null);
    }

    public static void setPlayerBlu3printConfig(String playerUUID, PlayerBlu3printConfig ppbc) {
        if (ppbc == null) {
            playerBlu3printConfig.remove(playerUUID);
        } else {
            playerBlu3printConfig.put(playerUUID, ppbc);
        }
    }

    public static Logger logger() {
        return instance.getLogger();
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

    @SuppressWarnings("unchecked")
    public void loadSavedBlueprintsToCache() {
        try {
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + "blueprints.json");
            if (!file.exists()) {
                File legacy = new File(getDataFolder().getAbsolutePath() + File.separator + "blu3prints.json");
                if (legacy.exists()) {
                    file = legacy;
                } else {
                    return;
                }
            }

            FileReader reader = new FileReader(file);
            Map<String, String> entries = gson.fromJson(reader, Map.class);
            if (entries == null || entries.isEmpty()) {
                getLogger().warning("No saved blu3prints found in blu3prints.json");
                return;
            }
            HashMap<String, Blu3printData> map = new HashMap<>();
            for (Entry<String, String> entry : entries.entrySet()) {
                Blu3printData data = new ImportedBlu3printData(null, entry.getValue(), entry.getKey());
                map.put(entry.getKey(), data);
            }
            getLogger().warning("Loaded Cached blu3prints from blu3prints.json");
            if (GlobalConfig.isImportedBlu3printsLoggingEnabled()) {
                map.forEach((k, v) -> getLogger().info(k + " : " + v.getEncodedString()));
            }
            cachedBlueprints.clear();
            cachedBlueprints.putAll(map);

        } catch (Exception e) {
            getLogger().severe("Failed to load blu3prints to cache : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void saveCachedBlu3prints() {
        try {
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + "blueprints.json");
            file.getParentFile().mkdir();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            Map<String, String> saveEncodings = new HashMap<>();
            cachedBlueprints.entrySet().forEach(entry -> {
                saveEncodings.put(entry.getKey(), entry.getValue().getEncodedString());
            });
            gson.toJson(saveEncodings, writer);
            writer.flush();
            writer.close();
            getLogger().warning("Cached blu3prints saved to blu3prints.json");
        } catch (Exception e) {
            getLogger().severe("Failed to save cached blu3prints : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Blu3printData getBlu3printFrpmCache(ItemStack blu3print, Player player) {
        String key = Blu3printItem.extractCacheKeyFromBlu3print(blu3print);
        if (key == null) {
            logger().warning("Blu3print ID is missing from the server cache");
            player.sendMessage("Blu3print ID is missing from the server cache");
        }
        return getBlu3printFrpmCache(key);
    }

    public Blu3printData getBlu3printFrpmCache(String key) {
        return cachedBlueprints.getOrDefault(key, null);
    }

    public String getKeyFromEncoding(String encoded) {
        return cachedBlueprints.entrySet().stream()
                .filter(e -> e.getValue().getEncodedString().equals(encoded))
                .map(Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public synchronized void saveOrUpdateCachedBlu3print(String key, Blu3printData data) {
        cachedBlueprints.put(key, data);
        saveCachedBlu3prints();
    }
}
