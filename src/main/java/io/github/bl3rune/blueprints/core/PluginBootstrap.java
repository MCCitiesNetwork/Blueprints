package io.github.bl3rune.blueprints.core;

import org.bukkit.scheduler.BukkitRunnable;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.enums.CommandType;
import io.github.bl3rune.blueprints.listeners.BookListener;
import io.github.bl3rune.blueprints.listeners.MenuInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerInteractListener;
import io.github.bl3rune.blueprints.listeners.PlayerJoinListener;
import io.github.bl3rune.blueprints.services.BlueprintCacheService;
import io.github.bl3rune.blueprints.services.DataFolderMigrator;
import io.github.bl3rune.blueprints.services.InteractionCooldownService;
import io.github.bl3rune.blueprints.services.PlayerSessionService;
import io.github.bl3rune.blueprints.services.RecipeRegistrar;
import io.github.bl3rune.blueprints.services.UpdateChecker;
import io.github.bl3rune.blueprints.services.domain.BlockApplicationStrategy;
import io.github.bl3rune.blueprints.services.domain.InventoryCostCalculator;
import io.github.bl3rune.blueprints.services.domain.LimitValidator;
import io.github.bl3rune.blueprints.services.domain.MaterialIgnoreResolver;
import io.github.bl3rune.blueprints.services.domain.PlacementPlanner;
import io.github.bl3rune.blueprints.services.persistence.BlueprintRepository;
import io.github.bl3rune.blueprints.services.persistence.JsonBlueprintRepository;

/**
 * Owns plugin startup and shutdown orchestration. {@link Blueprints} keeps
 * only the Bukkit lifecycle hooks and delegates all wiring to this class.
 *
 * <p>Phase 3 expands the registry contents: the blueprint cache, player
 * session state, and interaction cooldowns are now injectable services
 * instead of static maps.
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

        BlueprintRepository repository = new JsonBlueprintRepository(
                plugin.getDataFolder(), plugin.getLogger());
        registry.register(BlueprintRepository.class, repository);

        BlueprintCacheService cacheService = new BlueprintCacheService(repository, plugin.getLogger());
        cacheService.loadFromDisk();
        registry.register(BlueprintCacheService.class, cacheService);

        PlayerSessionService sessions = new PlayerSessionService();
        registry.register(PlayerSessionService.class, sessions);
        registry.register(InteractionCooldownService.class, new InteractionCooldownService());

        registry.register(MaterialIgnoreResolver.class, new MaterialIgnoreResolver(sessions));
        registry.register(PlacementPlanner.class, new PlacementPlanner());
        registry.register(LimitValidator.class, new LimitValidator());
        registry.register(InventoryCostCalculator.class, new InventoryCostCalculator(plugin.getLogger()));
        registry.register(BlockApplicationStrategy.class, new BlockApplicationStrategy(plugin.getLogger()));

        registerListeners();
        registerCommands();
        scheduleUpdateChecker();
    }

    public void stop() {
        plugin.getLogger().info("Stopping Blueprints");
        BlueprintCacheService cache = registry.find(BlueprintCacheService.class);
        if (cache != null) {
            cache.persist();
        }
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
