package io.github.bl3rune.blueprints;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerBlu3printConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ImportedBlu3printData;
import io.github.bl3rune.blueprints.enums.CommandType;
import io.github.bl3rune.blueprints.enums.SemanticLevel;
import io.github.bl3rune.blueprints.items.Blu3printItem;
import io.github.bl3rune.blueprints.listeners.BookListener;
import io.github.bl3rune.blueprints.listeners.MenuInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerJoinListener;

/**
 * The main class of the Blu3Print plugin
 * 
 * @author bl3rune
 */
public final class Blueprints extends JavaPlugin {

    // Static Fields
    private static Blueprints instance;
    private static List<String> updateMessages = new ArrayList<>();
    private static Map<String,PlayerConfig> playerConfig = new HashMap<>(); // Clears on server restart
    private static Map<String,PlayerBlu3printConfig> playerBlu3printConfig = new HashMap<>(); // Clears on server restart

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

    // Non-static
    private HashMap<String, Blu3printData> cachedBlueprints = new HashMap<>();
    private Gson gson = new Gson();

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getLogger().info("Starting Blueprints");
        migrateLegacyDataFolderIfNeeded();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        GlobalConfig.refreshConfiguration();
        addBlu3printRecipes();

        loadSavedBlueprintsToCache();

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new MenuInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BookListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        for (CommandType commandType : CommandType.values()) {
            getCommand(commandType.getFullCommandName()).setExecutor(commandType.getCommandExecutor());
            if (commandType.getTabCompleter() != null) {
                getCommand(commandType.getFullCommandName()).setTabCompleter(commandType.getTabCompleter());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }.runTaskTimer(this, 0, (60 * 60 * 20 * GlobalConfig.getUpdateCheckInterval())); // Run every X hours

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Stopping Blueprints");
        saveCachedBlu3prints();
        saveConfig();
    }

    public void checkUpdate() {
        try {
            SemanticLevel semanticLevel = GlobalConfig.getUpdateLoggingLevel();
            if (semanticLevel == SemanticLevel.NONE) {
                return;
            }

            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases/latest").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String latestVersion = json.get("tag_name").getAsString();
            String version = this.getDescription().getVersion();
            String[] lv = latestVersion.split("\\.");
            String[] v = version.split("\\.");
            boolean changed = lv.length != v.length;
            switch (semanticLevel) {
                case PATCH:
                    changed = changed || (!lv[2].equals(v[2]));
                case MINOR:
                    changed = changed || (!lv[1].equals(v[1]));
                case MAJOR:
                    changed = changed || (!lv[0].equals(v[0]));
                default:
            }
            if (changed) {
                updateMessages = new ArrayList<>();
                updateMessages.add("§9[Blu3Print]§r§6 New Update Available!");
                updateMessages.add("§9[Blu3Print]§r Current v" + version + " >> Latest v" + latestVersion);
                updateMessages.addAll(getUpdateMessageDetails());
                updateMessages.forEach(um -> Bukkit.getConsoleSender().sendMessage(um));
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for updates!");
            if (GlobalConfig.isVerboseLogging()) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> getUpdateMessageDetails() {
        SemanticLevel semanticLevel = GlobalConfig.getUpdateLoggingLevel();
        String version = this.getDescription().getVersion();
        String[] v = version.split("\\.");
        List<String> updateMessages = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray.asList()) {
                JsonObject release = element.getAsJsonObject();
                String releaseVersion = release.get("tag_name").getAsString();
                String[] rv = releaseVersion.split("\\.");
                boolean changed = rv.length != v.length;
                switch (semanticLevel) {
                    case PATCH:
                        changed = changed || (!rv[2].equals(v[2]));
                    case MINOR:
                        changed = changed || (!rv[1].equals(v[1]));
                    case MAJOR:
                        changed = changed || (!rv[0].equals(v[0]));
                    default:
                }
                if (!changed) {
                    break;
                }
                String update = release.get("name").getAsString();
                updateMessages.add("§9[Blu3Print]§r " + update);
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for detailed updates!");
        }

        return updateMessages;
    }

    private void addBlu3printRecipes() {

        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"),
                Blu3printItem.getBlankBlu3print());
        recipe.setGroup("Tools & Utilities");
        String recipeKey = getConfig().contains("blueprints.recipe.ingredients")
                ? "blueprints.recipe.ingredients"
                : "blu3print.recipe.ingredients";
        List<String> ingredients = getConfig().getStringList(recipeKey);

        try {
            for (String i : ingredients) {
                recipe.addIngredient(Material.valueOf(i));
            }
            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            getLogger().warning("Invalid blu3print recipe. using default recipe");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(this, "Blu3print_Writer"),
                    Blu3printItem.getBlankBlu3print());
            shapelessRecipe.addIngredient(Material.PAPER);
            shapelessRecipe.addIngredient(Material.LAPIS_LAZULI);
            shapelessRecipe.addIngredient(Material.FEATHER);
            Bukkit.addRecipe(shapelessRecipe);
        }
    }

    /**
     * Phase 1 migration: when the plugin descriptor name flipped from
     * {@code Blu3PrintPlugin} to {@code Blueprints}, Bukkit started using a
     * new data folder. If the new folder has no saved cache but the legacy
     * folder does, copy the file across so existing servers don't lose their
     * cached blueprints. The legacy file is left in place as a safety net.
     */
    private void migrateLegacyDataFolderIfNeeded() {
        try {
            File newFolder = getDataFolder();
            File legacyFolder = new File(newFolder.getParentFile(), "Blu3PrintPlugin");
            if (!legacyFolder.isDirectory()) {
                return;
            }
            if (!newFolder.isDirectory()) {
                newFolder.mkdirs();
            }
            File legacyData = new File(legacyFolder, "blu3prints.json");
            File newData = new File(newFolder, "blueprints.json");
            if (legacyData.isFile() && !newData.isFile()
                    && !new File(newFolder, "blu3prints.json").isFile()) {
                java.nio.file.Files.copy(legacyData.toPath(), newData.toPath());
                getLogger().info("Migrated cached blueprints from legacy "
                        + legacyData.getAbsolutePath() + " to " + newData.getAbsolutePath());
            }
            File legacyConfig = new File(legacyFolder, "config.yml");
            File newConfig = new File(newFolder, "config.yml");
            if (legacyConfig.isFile() && !newConfig.isFile()) {
                java.nio.file.Files.copy(legacyConfig.toPath(), newConfig.toPath());
                getLogger().info("Migrated legacy config.yml from "
                        + legacyConfig.getAbsolutePath() + " to " + newConfig.getAbsolutePath());
            }
        } catch (Exception e) {
            getLogger().warning("Failed to migrate legacy data folder: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSavedBlueprintsToCache() {
        try {
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + "blueprints.json");
            if (!file.exists()) {
                // Legacy filename fallback for the migration window.
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
            cachedBlueprints = map;

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
        return cachedBlueprints.entrySet().stream().filter(e -> e.getValue().getEncodedString().equals(encoded))
                .map(e -> e.getKey()).findFirst().orElse(null);
    }

    public synchronized void saveOrUpdateCachedBlu3print(String key, Blu3printData data) {
        cachedBlueprints.put(key, data);
        if (Blueprints.getInstance() != null) {
            Blueprints.getInstance().saveCachedBlu3prints();
        }
    }

}
