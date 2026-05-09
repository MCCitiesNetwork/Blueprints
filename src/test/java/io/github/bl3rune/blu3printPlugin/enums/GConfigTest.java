package io.github.bl3rune.blu3printPlugin.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Phase 0 behavior lockdown. Snapshots the exact config-key strings exposed
 * by every GConfig entry. The rename PR will need to map these to the new
 * "blueprints.*" namespace AND keep these old keys readable as a migration
 * shim — this test must be updated together with that change.
 */
class GConfigTest {

    @Test
    void allConfigPathsUseBlu3printRoot() {
        for (GConfig g : GConfig.values()) {
            assertTrue(g.getConfigPath().startsWith("blu3print."),
                    "expected blu3print.* root for " + g.name() + " but got " + g.getConfigPath());
        }
    }

    @Test
    void simpleEntriesDeriveKeyFromName() {
        assertEquals("blu3print.max-scale", GConfig.MAX_SCALE.getConfigPath());
        assertEquals("blu3print.max-size", GConfig.MAX_SIZE.getConfigPath());
        assertEquals("blu3print.max-overall-size", GConfig.MAX_OVERALL_SIZE.getConfigPath());
        assertEquals("blu3print.cooldown", GConfig.COOLDOWN.getConfigPath());
        assertEquals("blu3print.hologram-ttl", GConfig.HOLOGRAM_TTL.getConfigPath());
        assertEquals("blu3print.update-check-interval", GConfig.UPDATE_CHECK_INTERVAL.getConfigPath());
    }

    @Test
    void placementEntriesUseExplicitPath() {
        assertEquals("blu3print.placement.alignment", GConfig.ALIGNMENT.getConfigPath());
        assertEquals("blu3print.placement.relative", GConfig.RELATIVE.getConfigPath());
        assertEquals("blu3print.placement.force-place-penalty", GConfig.FORCE_PLACE_PENALTY.getConfigPath());
    }

    @Test
    void messagingEntriesUseExplicitPath() {
        assertEquals("blu3print.messaging.free-placement-message.enabled",
                GConfig.FREE_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blu3print.messaging.force-placement-message.enabled",
                GConfig.FORCED_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blu3print.messaging.discount-placement-message.enabled",
                GConfig.DISCOUNT_PLACEMENT_MESSAGE.getConfigPath());
        assertEquals("blu3print.messaging.update-available-message.enabled",
                GConfig.UPDATE_AVAILABLE_MESSAGE.getConfigPath());
        assertEquals("blu3print.messaging.cooldown-message.enabled",
                GConfig.COOLDOWN_MESSAGE.getConfigPath());
    }

    @Test
    void loggingEntriesUseExplicitPath() {
        assertEquals("blu3print.logging.verbose", GConfig.VERBOSE_LOGGING.getConfigPath());
        assertEquals("blu3print.logging.imported-blu3prints.enabled",
                GConfig.IMPORTED_BLU3PRINTS_LOGGING.getConfigPath());
        assertEquals("blu3print.logging.update-level", GConfig.UPDATE_LEVEL.getConfigPath());
    }

    @Test
    void totalEntryCountIsLocked() {
        // Lock the count so adding/removing entries forces a deliberate test update.
        assertEquals(17, GConfig.values().length);
    }
}
