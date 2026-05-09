package io.github.bl3rune.blu3printPlugin.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Phase 0 behavior lockdown for the per-blueprint Config enum. Locks which
 * keys are player-level vs blueprint-level so the config refactor cannot
 * silently flip a key's scope.
 */
class ConfigTest {

    @Test
    void blueprintLevelKeys() {
        assertFalse(Config.HOLOGRAM_VIEW_XYZ.isPlayerLevelConfig());
        assertFalse(Config.HOLOGRAM_VIEW_X.isPlayerLevelConfig());
        assertFalse(Config.HOLOGRAM_VIEW_Y.isPlayerLevelConfig());
        assertFalse(Config.HOLOGRAM_VIEW_Z.isPlayerLevelConfig());
    }

    @Test
    void playerLevelKeys() {
        assertTrue(Config.IGNORE_MATERIAL.isPlayerLevelConfig());
        assertTrue(Config.ALLOW_MATERIAL.isPlayerLevelConfig());
        assertTrue(Config.CLEAR.isPlayerLevelConfig());
    }

    @Test
    void totalEntryCountIsLocked() {
        assertEquals(7, Config.values().length);
    }
}
