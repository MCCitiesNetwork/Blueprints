package io.github.bl3rune.blueprints.config;

import java.util.List;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Static facade preserved for compatibility with existing call sites. After
 * Phase 6 of the architecture overhaul, the actual configuration state lives
 * on {@link ConfigService} as an immutable {@link ConfigSnapshot}; this
 * class simply forwards to the active snapshot. The static cache fields
 * that used to live here are gone.
 */
public final class GlobalConfig {

    private GlobalConfig() {
    }

    private static ConfigSnapshot snapshot() {
        Blueprints plugin = Blueprints.getInstance();
        if (plugin == null || plugin.getServiceRegistry() == null) {
            return ConfigSnapshot.empty();
        }
        ConfigService service = plugin.getServiceRegistry().find(ConfigService.class);
        return service == null ? ConfigSnapshot.empty() : service.current();
    }

    public static void refreshConfiguration() {
        Blueprints plugin = Blueprints.getInstance();
        if (plugin == null) {
            return;
        }
        ConfigService service = plugin.getServiceRegistry().find(ConfigService.class);
        if (service != null) {
            service.refresh(plugin);
        }
    }

    public static Integer getMaxSize() {
        return snapshot().maxSize;
    }

    public static Integer getMaxScale() {
        return snapshot().maxScale;
    }

    public static Integer getMaxOverallSize() {
        return snapshot().maxOverallSize;
    }

    public static Integer getCooldown() {
        Integer v = snapshot().cooldown;
        return v == null ? 0 : v;
    }

    public static Integer getHologramTtl() {
        Integer v = snapshot().hologramTtl;
        return v == null ? 10 : v;
    }

    public static List<String> getIgnoredMaterials() {
        return snapshot().ignoredMaterials;
    }

    public static Alignment getAlignment() {
        Alignment a = snapshot().alignment;
        return a == null ? Alignment.CENTER : a;
    }

    public static boolean getRelativePlacement() {
        return snapshot().relativePlacement;
    }

    public static boolean getForcePlacePenaltyEnabled() {
        return snapshot().forcePlacePenalty;
    }

    public static boolean isFreePlacementMessageEnabled() {
        return snapshot().freePlacementMessageEnabled;
    }

    public static boolean isForcePlacementMessageEnabled() {
        return snapshot().forcePlacementMessageEnabled;
    }

    public static boolean isDiscountPlacementMessageEnabled() {
        return snapshot().discountPlacementMessageEnabled;
    }

    public static boolean isUpdateAvailableMessageEnabled() {
        return snapshot().updateAvailableMessageEnabled;
    }

    public static boolean isCooldownMessageEnabled() {
        return snapshot().cooldownMessageEnabled;
    }

    public static boolean isVerboseLogging() {
        return snapshot().verboseLogging;
    }

    public static boolean isImportedBlu3printsLoggingEnabled() {
        return snapshot().importedBlu3printsLoggingEnabled;
    }

    public static SemanticLevel getUpdateLoggingLevel() {
        return snapshot().updateLoggingLevel;
    }

    public static int getUpdateCheckInterval() {
        Integer v = snapshot().updateCheckInterval;
        return v == null ? 24 : v;
    }

    public static boolean isForcePlacePenalty() {
        return snapshot().forcePlacePenalty;
    }
}
