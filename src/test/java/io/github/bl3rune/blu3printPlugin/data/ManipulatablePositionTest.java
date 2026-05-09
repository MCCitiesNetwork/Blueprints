package io.github.bl3rune.blu3printPlugin.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;
import io.github.bl3rune.blu3printPlugin.enums.Turn;
import io.github.bl3rune.blu3printPlugin.utils.Pair;

/**
 * Phase 0 lockdown for ManipulatablePosition. Locks size/scale getters,
 * the (z, y, x) constructor argument order, and a couple of turn outcomes
 * the placement engine relies on. The Phase 4 domain split must keep
 * these contracts intact.
 */
class ManipulatablePositionTest {

    @Test
    void constructorArgumentOrderIsZYX() {
        // Note the deliberate (z, y, x) ordering of the constructor.
        ManipulatablePosition p = new ManipulatablePosition(2, 3, 4, Orientation.NORTH, Rotation.TOP, 5);
        assertEquals(4, p.getXSize());
        assertEquals(3, p.getYSize());
        assertEquals(2, p.getZSize());
        assertEquals(Orientation.NORTH, p.getOrientation());
        assertEquals(Rotation.TOP, p.getRotation());
        assertEquals(5, p.getScale());
    }

    @Test
    void scalingIngredientsMultiplierIsCubed() {
        ManipulatablePosition p = new ManipulatablePosition(1, 1, 1, Orientation.NORTH, Rotation.TOP, 4);
        assertEquals(64, p.getScalingIngredientsMultiplier());
    }

    @Test
    void getNewSizesUnchangedForSameRotation() {
        ManipulatablePosition p = new ManipulatablePosition(2, 3, 4, Orientation.NORTH, Rotation.TOP);
        assertArrayEquals(new int[] {2, 3, 4}, p.getNewSizes(Rotation.TOP));
        // 180° opposite rotation also leaves sizes alone.
        assertArrayEquals(new int[] {2, 3, 4}, p.getNewSizes(Rotation.BOTTOM));
    }

    @Test
    void copyConstructorPreservesOrientationAndOverridesScale() {
        ManipulatablePosition base = new ManipulatablePosition(2, 3, 4, Orientation.EAST, Rotation.RIGHT, 1);
        ManipulatablePosition scaled = new ManipulatablePosition(base, 7);
        assertEquals(Orientation.EAST, scaled.getOrientation());
        assertEquals(Rotation.RIGHT, scaled.getRotation());
        assertEquals(7, scaled.getScale());
        // Sizes round-trip via the (z, y, x) ordering of the copy constructor.
        assertEquals(base.getXSize(), scaled.getXSize());
        assertEquals(base.getYSize(), scaled.getYSize());
        assertEquals(base.getZSize(), scaled.getZSize());
    }

    @Test
    void calculateTurnReturnsValidPair() {
        ManipulatablePosition p = new ManipulatablePosition(1, 1, 1, Orientation.NORTH, Rotation.TOP);
        // From NORTH/TOP, turning RIGHT goes EAST per the orientation matrix;
        // we don't lock the rotation since it's derived per-side, just that
        // a pair is returned and EAST is the new orientation.
        Pair<Orientation, Rotation> turned = p.calculateTurn(Turn.RIGHT);
        assertNotNull(turned);
        assertEquals(Orientation.EAST, turned.getA());
        assertNotNull(turned.getB());
    }

    @Test
    void iterationProducesAllPositionsWithoutDuplicates() {
        ManipulatablePosition p = new ManipulatablePosition(2, 2, 2, Orientation.NORTH, Rotation.TOP);
        java.util.Set<String> seen = new java.util.HashSet<>();
        int[] coord;
        int count = 0;
        while ((coord = p.next(false)) != null) {
            seen.add(coord[0] + "," + coord[1] + "," + coord[2]);
            count++;
            if (count > 100) break; // safety
        }
        assertEquals(8, count, "2x2x2 iteration should yield 8 positions");
        assertEquals(8, seen.size(), "all yielded positions should be distinct");
    }
}
