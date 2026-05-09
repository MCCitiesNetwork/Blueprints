package io.github.bl3rune.blueprints.config;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.GConfig;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Typed configuration service. Replaces the static field cache previously
 * held by {@code GlobalConfig} with an immutable {@link ConfigSnapshot}
 * rebuilt on each {@link #refresh(JavaPlugin)} call. Key strings are sourced
 * from {@link GConfig} so there is a single source of truth for every config
 * path; the legacy {@code blu3print.*} root is consulted only when the new
 * key is absent (Phase 1 migration window).
 *
 * <p>Parse failures fall back to the supplied default rather than throwing
 * — replacing the broad try/catch defensive blocks in the prior
 * implementation with explicit, per-call defaulting.
 */
public final class ConfigService {

    private final Logger logger;
    private volatile ConfigSnapshot current = ConfigSnapshot.empty();

    public ConfigService(Logger logger) {
        this.logger = logger;
    }

    public ConfigSnapshot current() {
        return current;
    }

    public void refresh(JavaPlugin plugin) {
        refresh(plugin.getConfig());
    }

    public void refresh(FileConfiguration config) {
        Integer maxSize = readInteger(config, GConfig.MAX_SIZE);
        Integer maxScale = readInteger(config, GConfig.MAX_SCALE);
        Integer maxOverallSize = readInteger(config, GConfig.MAX_OVERALL_SIZE);
        Integer cooldown = readInteger(config, GConfig.COOLDOWN);
        Integer hologramTtl = readInteger(config, GConfig.HOLOGRAM_TTL);
        Integer updateCheckInterval = readInteger(config, GConfig.UPDATE_CHECK_INTERVAL);
        List<String> ignoredMaterials = readStringList(config);

        boolean verbose = readBoolean(config, GConfig.VERBOSE_LOGGING, false);

        Alignment alignment = readEnum(config, GConfig.ALIGNMENT, Alignment.class, verbose);
        boolean relative = readBoolean(config, GConfig.RELATIVE, false);
        boolean forcePlacePenalty = readBoolean(config, GConfig.FORCE_PLACE_PENALTY, false);

        boolean freePlacement = readBoolean(config, GConfig.FREE_PLACEMENT_MESSAGE, false);
        boolean forcePlacement = readBoolean(config, GConfig.FORCED_PLACEMENT_MESSAGE, false);
        boolean discount = readBoolean(config, GConfig.DISCOUNT_PLACEMENT_MESSAGE, false);
        boolean updateAvailable = readBoolean(config, GConfig.UPDATE_AVAILABLE_MESSAGE, false);
        boolean cooldownMessage = readBoolean(config, GConfig.COOLDOWN_MESSAGE, false);
        boolean importedLogging = readBoolean(config, GConfig.IMPORTED_BLU3PRINTS_LOGGING, false);
        SemanticLevel updateLevel = readEnum(config, GConfig.UPDATE_LEVEL, SemanticLevel.class, verbose);

        this.current = new ConfigSnapshot(maxSize, maxScale, maxOverallSize, cooldown, hologramTtl,
                ignoredMaterials, alignment, relative, forcePlacePenalty, freePlacement, forcePlacement,
                discount, updateAvailable, cooldownMessage, verbose, importedLogging, updateLevel,
                updateCheckInterval);
    }

    private String resolvedKey(FileConfiguration config, GConfig entry) {
        if (config.contains(entry.getConfigPath())) {
            return entry.getConfigPath();
        }
        if (config.contains(entry.getLegacyConfigPath())) {
            return entry.getLegacyConfigPath();
        }
        return entry.getConfigPath();
    }

    private Integer readInteger(FileConfiguration config, GConfig entry) {
        String key = resolvedKey(config, entry);
        String value = config.getString(key, null);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warning("Config " + key + " is not a valid integer: '" + value + "'");
            return null;
        }
    }

    private boolean readBoolean(FileConfiguration config, GConfig entry, boolean fallback) {
        String key = resolvedKey(config, entry);
        return config.getBoolean(key, fallback);
    }

    private <T extends Enum<T>> T readEnum(FileConfiguration config, GConfig entry, Class<T> type, boolean verbose) {
        String key = resolvedKey(config, entry);
        String value = config.getString(key, null);
        if (value == null) {
            return null;
        }
        if (verbose) {
            logger.info("Config check : " + type.getName() + " : " + value);
        }
        for (T e : type.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                if (verbose) {
                    logger.info("Found : " + type.getName() + " : " + e.name());
                }
                return e;
            }
        }
        logger.info("Failed to get Enum " + type.getName());
        return null;
    }

    private List<String> readStringList(FileConfiguration config) {
        String newKey = "blueprints.ignored-materials";
        String legacyKey = "blu3print.ignored-materials";
        if (config.contains(newKey)) {
            return config.getStringList(newKey);
        }
        if (config.contains(legacyKey)) {
            return config.getStringList(legacyKey);
        }
        return Arrays.asList("AIR");
    }
}
