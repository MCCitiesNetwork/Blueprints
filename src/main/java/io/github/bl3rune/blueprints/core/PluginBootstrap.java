package io.github.bl3rune.blueprints.core;

import org.bukkit.scheduler.BukkitRunnable;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.enums.CommandType;
import io.github.bl3rune.blueprints.listeners.BookListener;
import io.github.bl3rune.blueprints.listeners.MenuInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerJoinListener;
import io.github.bl3rune.blueprints.services.DataFolderMigrator;
import io.github.bl3rune.blueprints.services.RecipeRegistrar;
import io.github.bl3rune.blueprints.services.UpdateChecker;

/**
 * Owns plugin startup and shutdown orchestration. {@link Blueprints} keeps
 * only the Bukkit lifecycle hooks and delegates all wiring to this class.
 *
 * <p>Phase 2 of the architecture overhaul introduces this seam so that
 * later phases can move runtime state (cache, player sessions, cooldowns)
 * into injected services without further surgery on the plugin main class.
 */
public final class PluginBootstrap {

    private final Blueprints plugin;
    private final ServiceRegistry registry = new ServiceRegistry();

    public PluginBootstrap(Blueprints plugin) {
        this.plugin = plugin;
    }

    public ServiceRegistry getRegistry() {
        return registry;
    }

    public void start() {
        plugin.getLogger().info("Starting Blueprints");

        DataFolderMigrator migrator = new DataFolderMigrator();
        migrator.migrateIfNeeded(plugin);
        registry.register(DataFolderMigrator.class, migrator);

        plugin.getConfig().options().copyDefaults();
        plugin.saveDefaultConfig();
        GlobalConfig.refreshConfiguration();

        RecipeRegistrar recipeRegistrar = new RecipeRegistrar();
        recipeRegistrar.register(plugin);
        registry.register(RecipeRegistrar.class, recipeRegistrar);

        plugin.loadSavedBlueprintsToCache();

        registerListeners();
        registerCommands();
        scheduleUpdateChecker();
    }

    public void stop() {
        plugin.getLogger().info("Stopping Blueprints");
        plugin.saveCachedBlu3prints();
        plugin.saveConfig();
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MenuInteractListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BookListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
    }

    private void registerCommands() {
        for (CommandType commandType : CommandType.values()) {
            plugin.getCommand(commandType.getFullCommandName()).setExecutor(commandType.getCommandExecutor());
            if (commandType.getTabCompleter() != null) {
                plugin.getCommand(commandType.getFullCommandName()).setTabCompleter(commandType.getTabCompleter());
            }
        }
    }

    private void scheduleUpdateChecker() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, Blueprints::getUpdateMessages);
        registry.register(UpdateChecker.class, updateChecker);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateChecker.checkUpdate();
            }
        }.runTaskTimer(plugin, 0, (60L * 60 * 20 * GlobalConfig.getUpdateCheckInterval()));
    }
}
