package io.github.bl3rune.blueprints.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Phase 8 coverage for the immutable config value type. These tests lock the
 * snapshot's null-tolerance, list-defensive-copy, and {@link ConfigSnapshot#empty()}
 * defaults so future edits cannot quietly turn the type mutable or change the
 * empty-state contract that {@code GlobalConfig} relies on for pre-bootstrap
 * lookups.
 */
class ConfigSnapshotTest {

    @Test
    void emptySnapshotMatchesGlobalConfigPreBootstrapDefaults() {
        ConfigSnapshot empty = ConfigSnapshot.empty();

        assertNull(empty.maxSize);
        assertNull(empty.maxScale);
        assertNull(empty.maxOverallSize);
        assertNull(empty.cooldown);
        assertNull(empty.hologramTtl);
        assertNull(empty.alignment);
        assertNull(empty.updateLoggingLevel);
        assertNull(empty.updateCheckInterval);

        assertNotNull(empty.ignoredMaterials);
        assertTrue(empty.ignoredMaterials.isEmpty());

        assertFalse(empty.relativePlacement);
        assertTrue(empty.forcePlacePenalty,
                "force-place-penalty defaults to true (matches the cached static-init value pre-Phase 6)");
        assertFalse(empty.freePlacementMessageEnabled);
        assertFalse(empty.forcePlacementMessageEnabled);
        assertFalse(empty.discountPlacementMessageEnabled);
        assertFalse(empty.updateAvailableMessageEnabled);
        assertFalse(empty.cooldownMessageEnabled);
        assertFalse(empty.verboseLogging);
        assertFalse(empty.importedBlu3printsLoggingEnabled);
    }

    @Test
    void ignoredMaterialsIsUnmodifiable() {
        List<String> source = new ArrayList<>(Arrays.asList("AIR", "WATER"));
        ConfigSnapshot snapshot = make(source);

        assertEquals(Arrays.asList("AIR", "WATER"), snapshot.ignoredMaterials);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.ignoredMaterials.add("LAVA"),
                "snapshot list must not be mutable from outside");
    }

    @Test
    void ignoredMaterialsIsIsolatedFromCallerMutation() {
        List<String> source = new ArrayList<>(Arrays.asList("AIR"));
        ConfigSnapshot snapshot = make(source);

        // The wrapping is unmodifiableList — mutating the source list still
        // shows through (that is unmodifiableList's documented behavior).
        // What the snapshot guarantees is that *consumers* cannot mutate.
        // This test pins that contract so future changes do not silently
        // switch to a deep copy (which would be fine but is a real change).
        source.add("WATER");
        assertEquals(Arrays.asList("AIR", "WATER"), snapshot.ignoredMaterials);
    }

    @Test
    void nullIgnoredMaterialsBecomesEmptyList() {
        ConfigSnapshot snapshot = make(null);

        assertNotNull(snapshot.ignoredMaterials);
        assertTrue(snapshot.ignoredMaterials.isEmpty());
    }

    @Test
    void everyFieldIsSetByConstructor() {
        ConfigSnapshot snapshot = new ConfigSnapshot(
                10, 4, 256, 5, 30,
                Arrays.asList("AIR"),
                Alignment.CENTER, true, false,
                true, true, true, true, true,
                true, true, SemanticLevel.MAJOR, 12);

        assertEquals(10, snapshot.maxSize);
        assertEquals(4, snapshot.maxScale);
        assertEquals(256, snapshot.maxOverallSize);
        assertEquals(5, snapshot.cooldown);
        assertEquals(30, snapshot.hologramTtl);
        assertEquals(Alignment.CENTER, snapshot.alignment);
        assertTrue(snapshot.relativePlacement);
        assertFalse(snapshot.forcePlacePenalty);
        assertTrue(snapshot.freePlacementMessageEnabled);
        assertTrue(snapshot.forcePlacementMessageEnabled);
        assertTrue(snapshot.discountPlacementMessageEnabled);
        assertTrue(snapshot.updateAvailableMessageEnabled);
        assertTrue(snapshot.cooldownMessageEnabled);
        assertTrue(snapshot.verboseLogging);
        assertTrue(snapshot.importedBlu3printsLoggingEnabled);
        assertEquals(SemanticLevel.MAJOR, snapshot.updateLoggingLevel);
        assertEquals(12, snapshot.updateCheckInterval);
    }

    private static ConfigSnapshot make(List<String> ignored) {
        return new ConfigSnapshot(null, null, null, null, null,
                ignored, null, false, true, false, false, false, false, false,
                false, false, null, null);
    }
}
