package io.github.bl3rune.blu3printPlugin.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

/**
 * Phase 0 lockdown for directional enums (Direction, Rotation, Turn,
 * Orientation). Placement maths in ManipulatablePosition rely on these
 * exact values; the refactor must not perturb them.
 */
class DirectionalEnumsTest {

    @Test
    void directionPosIndexAndInversion() {
        assertEquals(2, Direction.X_POS.getPosIndex());
        assertEquals(2, Direction.X_NEG.getPosIndex());
        assertEquals(1, Direction.Y_POS.getPosIndex());
        assertEquals(1, Direction.Y_NEG.getPosIndex());
        assertEquals(0, Direction.Z_POS.getPosIndex());
        assertEquals(0, Direction.Z_NEG.getPosIndex());

        assertEquals(Direction.X_NEG, Direction.X_POS.getInverted());
        assertEquals(Direction.Y_NEG, Direction.Y_POS.getInverted());
        assertEquals(Direction.Z_NEG, Direction.Z_POS.getInverted());
        assertEquals(Direction.X_POS, Direction.X_NEG.getInverted());

        assertTrue(Direction.X_NEG.isNegative());
        assertFalse(Direction.X_POS.isNegative());
    }

    @Test
    void rotationCycle() {
        assertEquals(Rotation.RIGHT, Rotation.TOP.getNextRotation());
        assertEquals(Rotation.BOTTOM, Rotation.RIGHT.getNextRotation());
        assertEquals(Rotation.LEFT, Rotation.BOTTOM.getNextRotation());
        assertEquals(Rotation.TOP, Rotation.LEFT.getNextRotation());

        assertEquals(Rotation.BOTTOM, Rotation.TOP.getOpposite());
        assertEquals(Rotation.LEFT, Rotation.RIGHT.getOpposite());

        assertTrue(Rotation.RIGHT.isHorizontal());
        assertTrue(Rotation.LEFT.isHorizontal());
        assertFalse(Rotation.TOP.isHorizontal());
        assertFalse(Rotation.BOTTOM.isHorizontal());
    }

    @Test
    void rotationFromCodeFallsBackToTop() {
        assertEquals(Rotation.TOP, Rotation.fromCode("0"));
        assertEquals(Rotation.RIGHT, Rotation.fromCode("1"));
        assertEquals(Rotation.BOTTOM, Rotation.fromCode("2"));
        assertEquals(Rotation.LEFT, Rotation.fromCode("3"));
        assertEquals(Rotation.TOP, Rotation.fromCode("garbage"));
        assertEquals(Rotation.TOP, Rotation.fromCode(""));
    }

    @Test
    void turnPlusRotation() {
        assertEquals(Turn.RIGHT, Turn.UP.plusRotation(Rotation.RIGHT));
        assertEquals(Turn.DOWN, Turn.UP.plusRotation(Rotation.BOTTOM));
        assertTrue(Turn.RIGHT.isHorizontal());
        assertTrue(Turn.UP.isVertical());
    }

    @Test
    void orientationDescriptionIsFirstLetter() {
        assertEquals("N", Orientation.NORTH.getDescription());
        assertEquals("S", Orientation.SOUTH.getDescription());
        assertEquals("E", Orientation.EAST.getDescription());
        assertEquals("W", Orientation.WEST.getDescription());
        assertEquals("U", Orientation.UP.getDescription());
        assertEquals("D", Orientation.DOWN.getDescription());
    }

    @Test
    void orientationGetOrientationByDescriptionFallsBackToSouth() {
        assertEquals(Orientation.NORTH, Orientation.getOrientation("N"));
        assertEquals(Orientation.SOUTH, Orientation.getOrientation("S"));
        assertEquals(Orientation.SOUTH, Orientation.getOrientation("nonsense"));
    }

    @Test
    void orientationGetOrientationByBlockFaceFallsBackToSouth() {
        assertEquals(Orientation.EAST, Orientation.getOrientation(BlockFace.EAST));
        // Compound directions collapse to their cartesian face.
        assertEquals(Orientation.NORTH, Orientation.getOrientation(BlockFace.NORTH_EAST));
        assertEquals(Orientation.SOUTH, Orientation.getOrientation(BlockFace.SELF));
    }

    @Test
    void orientationCompassRotation() {
        assertTrue(Orientation.NORTH.isCompass());
        assertFalse(Orientation.UP.isCompass());

        assertEquals(Orientation.EAST, Orientation.NORTH.getNextCompass());
        assertEquals(Orientation.SOUTH, Orientation.EAST.getNextCompass());
        assertEquals(Orientation.WEST, Orientation.SOUTH.getNextCompass());
        assertEquals(Orientation.NORTH, Orientation.WEST.getNextCompass());

        // getNextOrientation cycles through all six (vs only compass).
        assertNotEquals(Orientation.NORTH, Orientation.NORTH.getNextOrientation());
    }

    @Test
    void orientationOpposite() {
        assertEquals(Orientation.SOUTH, Orientation.NORTH.getOpposite());
        assertEquals(Orientation.WEST, Orientation.EAST.getOpposite());
        assertEquals(Orientation.DOWN, Orientation.UP.getOpposite());
    }

    @Test
    void orientationDirectionalStrings() {
        // These get baked into encoded blueprint headers — locking the format.
        assertEquals("facing=north", Orientation.NORTH.getDirectional());
        assertEquals("f=n", Orientation.NORTH.getDirectionalShort());
        assertEquals("rotation=8", Orientation.NORTH.getRotatable());
        assertEquals("r=8", Orientation.NORTH.getRotatableShort());
    }
}
