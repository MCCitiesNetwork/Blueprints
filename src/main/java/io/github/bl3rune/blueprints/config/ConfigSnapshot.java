package io.github.bl3rune.blueprints.config;

import java.util.Collections;
import java.util.List;

import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Immutable snapshot of every runtime config value. After Phase 6, this
 * replaces the bag of mutable static fields previously held by
 * {@code GlobalConfig}. {@link ConfigService} produces a new snapshot on
 * each refresh; {@code GlobalConfig} static getters delegate to the active
 * snapshot for compat with existing call sites.
 */
public final class ConfigSnapshot {

    public final Integer maxSize;
    public final Integer maxScale;
    public final Integer maxOverallSize;
    public final Integer cooldown;
    public final Integer hologramTtl;
    public final List<String> ignoredMaterials;

    public final Alignment alignment;
    public final boolean relativePlacement;
    public final boolean forcePlacePenalty;

    public final boolean freePlacementMessageEnabled;
    public final boolean forcePlacementMessageEnabled;
    public final boolean discountPlacementMessageEnabled;
    public final boolean updateAvailableMessageEnabled;
    public final boolean cooldownMessageEnabled;

    public final boolean verboseLogging;
    public final boolean importedBlu3printsLoggingEnabled;
    public final SemanticLevel updateLoggingLevel;

    public final Integer updateCheckInterval;

    public ConfigSnapshot(Integer maxSize, Integer maxScale, Integer maxOverallSize, Integer cooldown,
            Integer hologramTtl, List<String> ignoredMaterials, Alignment alignment, boolean relativePlacement,
            boolean forcePlacePenalty, boolean freePlacementMessageEnabled, boolean forcePlacementMessageEnabled,
            boolean discountPlacementMessageEnabled, boolean updateAvailableMessageEnabled,
            boolean cooldownMessageEnabled, boolean verboseLogging, boolean importedBlu3printsLoggingEnabled,
            SemanticLevel updateLoggingLevel, Integer updateCheckInterval) {
        this.maxSize = maxSize;
        this.maxScale = maxScale;
        this.maxOverallSize = maxOverallSize;
        this.cooldown = cooldown;
        this.hologramTtl = hologramTtl;
        this.ignoredMaterials = ignoredMaterials == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(ignoredMaterials);
        this.alignment = alignment;
        this.relativePlacement = relativePlacement;
        this.forcePlacePenalty = forcePlacePenalty;
        this.freePlacementMessageEnabled = freePlacementMessageEnabled;
        this.forcePlacementMessageEnabled = forcePlacementMessageEnabled;
        this.discountPlacementMessageEnabled = discountPlacementMessageEnabled;
        this.updateAvailableMessageEnabled = updateAvailableMessageEnabled;
        this.cooldownMessageEnabled = cooldownMessageEnabled;
        this.verboseLogging = verboseLogging;
        this.importedBlu3printsLoggingEnabled = importedBlu3printsLoggingEnabled;
        this.updateLoggingLevel = updateLoggingLevel;
        this.updateCheckInterval = updateCheckInterval;
    }

    public static ConfigSnapshot empty() {
        return new ConfigSnapshot(null, null, null, null, null, Collections.emptyList(),
                null, false, true, false, false, false, false, false, false, false, null, null);
    }
}
