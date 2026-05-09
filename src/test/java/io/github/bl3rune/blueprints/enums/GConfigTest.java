package io.github.bl3rune.blueprints.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Snapshots the exact config-key strings exposed by every GConfig entry.
 * Phase 1 moved the canonical root to {@code blueprints.*}; the legacy
 * {@code blu3print.*} root stays readable through {@code getLegacyConfigPath()}
 * for the migration window.
 */
class GConfigTest {

    @Test
    void allConfigPathsUseBlueprintsRoot() {
        for (GConfig g : GConfig.values()) {
            assertTrue(g.getConfigPath().startsWith("blueprints."),
                    "expected blueprints.* root for " + g.name() + " but got " + g.getConfigPath());
        }
    }

    @Test
    void everyEntryAlsoExposesLegacyBlu3printPath() {
        // Migration shim: every key MUST also be reachable under the legacy
        // root so existing operator configs keep loading until the window ends.
        for (GConfig g : GConfig.values()) {
            assertTrue(g.getLegacyConfigPath().startsWith("blu3print."),
                    "expected blu3print.* legacy path for " + g.name()
                            + " but got " + g.getLegacyConfigPath());
            // The two paths only differ in their root segment.
            String suffixNew = g.getConfigPath().substring("blueprints.".length());
            String suffixOld = g.getLegacyConfigPath().substring("blu3print.".length());
            assertEquals(suffixNew, suffixOld);
        }
    }

    @Test
    void simpleEntriesDeriveKeyFromName() {
        assertEquals("blueprints.max-scale", GConfig.MAX_SCALE.getConfigPath());
        assertEquals("blueprints.max-size", GConfig.MAX_SIZE.getConfigPath());
        assertEquals("blueprints.max-overall-size", GConfig.MAX_OVERALL_SIZE.getConfigPath());
        assertEquals("blueprints.cooldown", GConfig.COOLDOWN.getConfigPath());
        assertEquals("blueprints.hologram-ttl", GConfig.HOLOGRAM_TTL.getConfigPath());
        assertEquals("blueprints.update-check-interval", GConfig.UPDATE_CHECK_INTERVAL.getConfigPath());
    }

    @Test
    void placementEntriesUseExplicitPath() {
        assertEquals("blueprints.placement.alignment", GConfig.ALIGNMENT.getConfigPath());
        assertEquals("blueprints.placement.relative", GConfig.RELATIVE.getConfigPath());
        assertEquals("blueprints.placement.force-place-penalty", GConfig.FORCE_PLACE_PENALTY.getConfigPath());
    }

    @Test
    void messagingEntriesUseExplicitPath() {
        assertEquals("blueprints.messaging.free-placement-message.enabled",
                GConfig.FREE_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blueprints.messaging.force-placement-message.enabled",
                GConfig.FORCED_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blueprints.messaging.discount-placement-message.enabled",
                GConfig.DISCOUNT_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blueprints.messaging.update-available-message.enabled",
                GConfig.UPDATE_AVAILABLE_MESSAGE.getConfigPath());
        assertEquals("blueprints.messaging.cooldown-message.enabled",
                GConfig.COOLDOWN_MESSAGE.getConfigPath());
    }

    @Test
    void loggingEntriesUseExplicitPath() {
        assertEquals("blueprints.logging.verbose", GConfig.VERBOSE_LOGGING.getConfigPath());
        assertEquals("blueprints.logging.imported-blu3prints.enabled",
                GConfig.IMPORTED_BLU3PRINTS_LOGGING.getConfigPath());
        assertEquals("blueprints.logging.update-level", GConfig.UPDATE_LEVEL.getConfigPath());
    }

    @Test
    void totalEntryCountIsLocked() {
        // Lock the count so adding/removing entries forces a deliberate test update.
        assertEquals(17, GConfig.values().length);
    }
}
