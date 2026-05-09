package io.github.bl3rune.blueprints.enums;

import java.util.function.Supplier;

import io.github.bl3rune.blueprints.config.GlobalConfig;

public enum GConfig {
    MAX_SCALE(GlobalConfig::getMaxScale),
    MAX_SIZE(GlobalConfig::getMaxSize),
    MAX_OVERALL_SIZE(GlobalConfig::getMaxOverallSize),
    COOLDOWN(GlobalConfig::getCooldown),
    HOLOGRAM_TTL(GlobalConfig::getHologramTtl),
    // Placement
    ALIGNMENT(GlobalConfig::getAlignment, "placement.alignment"),
    RELATIVE(GlobalConfig::getRelativePlacement, "placement.relative"),
    FORCE_PLACE_PENALTY(GlobalConfig::getForcePlacePenaltyEnabled, "placement.force-place-penalty"),
    // Messaging
    FREE_PLACEMENT_MESSAGE(GlobalConfig::isFreePlacementMessageEnabled, "messaging.free-placement-message.enabled"),
    FORCED_PLACEMENT_MESSAGE(GlobalConfig::isForcePlacementMessageEnabled, "messaging.force-placement-message.enabled"),
    DISCOUNT_PLACEMENT_MESSAGE(GlobalConfig::isDiscountPlacementMessageEnabled, "messaging.discount-placement-message.enabled"),
    UPDATE_AVAILABLE_MESSAGE(GlobalConfig::isUpdateAvailableMessageEnabled, "messaging.update-available-message.enabled"),
    COOLDOWN_MESSAGE(GlobalConfig::isCooldownMessageEnabled, "messaging.cooldown-message.enabled"),
    // Logging
    VERBOSE_LOGGING(GlobalConfig::isVerboseLogging, "logging.verbose"),
    IMPORTED_BLU3PRINTS_LOGGING(GlobalConfig::isImportedBlu3printsLoggingEnabled, "logging.imported-blu3prints.enabled"),
    UPDATE_LEVEL(GlobalConfig::getUpdateLoggingLevel, "logging.update-level"),
    //  Other
    UPDATE_CHECK_INTERVAL(GlobalConfig::getUpdateCheckInterval),
    
    ;

    public static final String CONFIG_ROOT = "blueprints";
    public static final String LEGACY_CONFIG_ROOT = "blu3print";

    private final String configKey;
    private final Supplier<Object> getter;

    GConfig(Supplier<Object> getter, String path) {
        this.getter = getter;
        this.configKey = path.toLowerCase();
    }

    GConfig(Supplier<Object> getter) {
        this.getter = getter;
        this.configKey = name().toLowerCase().replace('_', '-');
    }

    /**
     * The canonical config path under the new {@code blueprints.*} root.
     */
    public String getConfigPath() {
        return CONFIG_ROOT + "." + configKey;
    }

    /**
     * The legacy config path under the old {@code blu3print.*} root, kept
     * during the Phase 1 migration window so existing configs keep loading.
     */
    public String getLegacyConfigPath() {
        return LEGACY_CONFIG_ROOT + "." + configKey;
    }

    public String getCurrentValue() {
        return "" + getter.get();
    }
}
