package io.github.bl3rune.blueprints.core;

import org.bukkit.scheduler.BukkitRunnable;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.commands.BlueprintCommand;
import io.github.bl3rune.blueprints.commands.ConfigCommand;
import io.github.bl3rune.blueprints.commands.ConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.DuplicateCommand;
import io.github.bl3rune.blueprints.commands.ExportCommand;
import io.github.bl3rune.blueprints.commands.FaceCommand;
import io.github.bl3rune.blueprints.commands.FaceTabCompleter;
import io.github.bl3rune.blueprints.commands.GiveCommand;
import io.github.bl3rune.blueprints.commands.GiveTabCompleter;
import io.github.bl3rune.blueprints.commands.GlobalConfigCommand;
import io.github.bl3rune.blueprints.commands.GlobalConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.HelpCommand;
import io.github.bl3rune.blueprints.commands.ImportCommand;
import io.github.bl3rune.blueprints.commands.NameCommand;
import io.github.bl3rune.blueprints.commands.PlayerConfigCommand;
import io.github.bl3rune.blueprints.commands.PlayerConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.RotateCommand;
import io.github.bl3rune.blueprints.commands.RotateTabCompleter;
import io.github.bl3rune.blueprints.commands.ScaleCommand;
import io.github.bl3rune.blueprints.commands.ScaleTabCompleter;
import io.github.bl3rune.blueprints.commands.TurnCommand;
import io.github.bl3rune.blueprints.commands.TurnTabCompleter;
import io.github.bl3rune.blueprints.config.ConfigService;
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

        ConfigService configService = new ConfigService(plugin.getLogger());
        registry.register(ConfigService.class, configService);
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

        CommandRegistry commandRegistry = buildCommandRegistry();
        registry.register(CommandRegistry.class, commandRegistry);

        registerListeners();
        registerCommands(commandRegistry);
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
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MenuInteractListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BookListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
    }

    private CommandRegistry buildCommandRegistry() {
        CommandRegistry r = new CommandRegistry();
        r.register(CommandType.BLU3PRINT, new BlueprintCommand());
        r.register(CommandType.DUPLICATE, new DuplicateCommand());
        r.register(CommandType.FACE, new FaceCommand(), new FaceTabCompleter());
        r.register(CommandType.ROTATE, new RotateCommand(), new RotateTabCompleter());
        r.register(CommandType.TURN, new TurnCommand(), new TurnTabCompleter());
        r.register(CommandType.IMPORT, new ImportCommand(plugin));
        r.register(CommandType.EXPORT, new ExportCommand());
        r.register(CommandType.SCALE, new ScaleCommand(), new ScaleTabCompleter());
        r.register(CommandType.NAME, new NameCommand());
        r.register(CommandType.GIVE, new GiveCommand(plugin), new GiveTabCompleter());
        r.register(CommandType.HELP, new HelpCommand());
        r.register(CommandType.CONFIG, new ConfigCommand(), new ConfigTabCompleter());
        r.register(CommandType.PLAYER, new PlayerConfigCommand(), new PlayerConfigTabCompleter());
        r.register(CommandType.GLOBAL, new GlobalConfigCommand(), new GlobalConfigTabCompleter());
        return r;
    }

    private void registerCommands(CommandRegistry commandRegistry) {
        for (CommandType commandType : CommandType.values()) {
            plugin.getCommand(commandType.getFullCommandName())
                    .setExecutor(commandRegistry.getExecutor(commandType));
            if (commandType.hasTabCompleter()) {
                plugin.getCommand(commandType.getFullCommandName())
                        .setTabCompleter(commandRegistry.getTabCompleter(commandType));
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
