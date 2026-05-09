package io.github.bl3rune.blueprints.config;

import java.util.Arrays;
import java.util.List;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

import static io.github.bl3rune.blueprints.Blueprints.logger;

public class GlobalConfig {
    
    private static Integer maxSize = null;
    private static Integer maxScale = null;
    private static Integer maxOverallSize = null;
    private static Integer cooldown = null;
    private static Integer hologramTtl = null;
    private static List<String> ignoredMaterials;
    // Placement
    private static Alignment alignment = null;
    private static boolean relativePlacement = false;
    private static boolean forcePlacePenalty = true;
    // Messages
    private static boolean freePlacementMessageEnabled = false;
    private static boolean forcePlacementMessageEnabled = false;
    private static boolean discountPlacementMessageEnabled = false;
    private static boolean updateAvailableMessageEnabled = false;
    private static boolean cooldownMessageEnabled = false;
    // Logging
    private static boolean verboseLogging = false;
    private static boolean importedBlu3printsLoggingEnabled = false;
    private static SemanticLevel updateLoggingLevel = SemanticLevel.MINOR;
    // Other
    private static Integer updateCheckInterval = null;

    /**
     * Phase 1 migration: read the new {@code blueprints.*} root first, but
     * fall back to the legacy {@code blu3print.*} root if a key is missing.
     * Lets existing operator configs keep loading during the transition.
     */
    private static String resolveKey(String suffix) {
        String newKey = "blueprints." + suffix;
        if (Blueprints.getInstance().getConfig().contains(newKey)) {
            return newKey;
        }
        String legacyKey = "blu3print." + suffix;
        if (Blueprints.getInstance().getConfig().contains(legacyKey)) {
            return legacyKey;
        }
        return newKey;
    }

    public static void refreshConfiguration() {
        verboseLogging = tryAndGetConfigFlag(resolveKey("logging.verbose"));

        maxSize = tryAndGetConfigInteger(resolveKey("max-size"));
        maxScale = tryAndGetConfigInteger(resolveKey("max-scale"));
        maxOverallSize = tryAndGetConfigInteger(resolveKey("max-overall-size"));
        cooldown = tryAndGetConfigInteger(resolveKey("cooldown"));
        hologramTtl = tryAndGetConfigInteger(resolveKey("hologram-ttl"));
        ignoredMaterials = tryAndGetConfigList(resolveKey("ignored-materials"));
        // Placement settings
        alignment = tryAndGetConfigEnum(resolveKey("placement.alignment"), Alignment.class);
        relativePlacement = tryAndGetConfigFlag(resolveKey("placement.relative"));
        forcePlacePenalty = tryAndGetConfigFlag(resolveKey("placement.force-place-penalty"));
        // Message settings
        freePlacementMessageEnabled = tryAndGetConfigFlag(resolveKey("messaging.free-placement-message.enabled"));
        forcePlacementMessageEnabled = tryAndGetConfigFlag(resolveKey("messaging.force-placement-message.enabled"));
        discountPlacementMessageEnabled = tryAndGetConfigFlag(resolveKey("messaging.discount-placement-message.enabled"));
        updateAvailableMessageEnabled = tryAndGetConfigFlag(resolveKey("messaging.update-available-message.enabled"));
        cooldownMessageEnabled = tryAndGetConfigFlag(resolveKey("messaging.cooldown-message.enabled"));
        // Logging settings
        verboseLogging = tryAndGetConfigFlag(resolveKey("logging.verbose"));
        importedBlu3printsLoggingEnabled = tryAndGetConfigFlag(resolveKey("logging.imported-blu3prints.enabled"));
        updateLoggingLevel = tryAndGetConfigEnum(resolveKey("logging.update-level"), SemanticLevel.class);

        // Other settings
        updateCheckInterval = tryAndGetConfigInteger(resolveKey("update-check-interval"));


    }

    private static Integer tryAndGetConfigInteger(String key) {
        try {
            String value = Blueprints.getInstance().getConfig().getString(key, null);
            return value == null ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T extends Enum<T>> T tryAndGetConfigEnum(String key, Class<T> clazz) {
        try {
            if (clazz.isEnum()) {
                String value = Blueprints.getInstance().getConfig().getString(key, null);
                if (isVerboseLogging()) {
                    logger().info("Config check : " + clazz.getName() + " : " + value);
                }
                for  (T e : clazz.getEnumConstants()) {
                    if (e.name().equalsIgnoreCase(value)) {
                        if (isVerboseLogging()) {
                            logger().info("Found : " + clazz.getName() + " : " + e.name());
                        }
                        return e;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger().info("Failed to get Enum " + clazz.getName());
            if (isVerboseLogging()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static List<String> tryAndGetConfigList(String key) {
        try {
            return Blueprints.getInstance().getConfig().getStringList(key);
        } catch (Exception e) {
            return Arrays.asList("AIR");
        }
    }

    private static boolean tryAndGetConfigFlag(String key) {
        try {
            return Blueprints.getInstance().getConfig().getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static Integer getMaxSize() {
        return maxSize;
    }

    public static Integer getMaxScale() {
        return maxScale;
    }

    public static Integer getMaxOverallSize() {
        return maxOverallSize;
    }

    public static Integer getCooldown() {
        if (cooldown == null) {
            return 0;
        }
        return cooldown;
    }

    public static Integer getHologramTtl() {
        if (hologramTtl == null) {
            return 10;
        }
        return hologramTtl;
    }

    public static List<String> getIgnoredMaterials() {
        return ignoredMaterials;
    }

    public static Alignment getAlignment() {
        if (alignment == null) {
            return Alignment.CENTER;
        }
        return alignment;
    }

    public static boolean getRelativePlacement() {
        return relativePlacement;
    }

    public static boolean getForcePlacePenaltyEnabled() {
        return forcePlacePenalty;
    }

    public static boolean isFreePlacementMessageEnabled() {
        return freePlacementMessageEnabled;
    }

    public static boolean isForcePlacementMessageEnabled() {
        return forcePlacementMessageEnabled;
    }

    public static boolean isDiscountPlacementMessageEnabled() {
        return discountPlacementMessageEnabled;
    }

    public static boolean isUpdateAvailableMessageEnabled() {
        return updateAvailableMessageEnabled;
    }

    public static boolean isCooldownMessageEnabled() {
        return cooldownMessageEnabled;
    }

    public static boolean isVerboseLogging() {
        return verboseLogging;
    }

    public static boolean isImportedBlu3printsLoggingEnabled() {
        return importedBlu3printsLoggingEnabled;
    }

    public static SemanticLevel getUpdateLoggingLevel() {
        return updateLoggingLevel;
    }

    public static int getUpdateCheckInterval() {
        if (updateCheckInterval == null) {
            return 24;
        }
        return updateCheckInterval;
    }

    public static boolean isForcePlacePenalty() {
        return forcePlacePenalty;
    }
}
