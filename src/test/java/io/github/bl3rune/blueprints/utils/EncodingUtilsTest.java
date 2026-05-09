package io.github.bl3rune.blueprints.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.Rotation;

/**
 * Phase 0 behavior lockdown. Captures the encoding contract exactly as it
 * exists today so the rename + refactor cannot drift it. Any change to these
 * outputs is a behavior change and requires its own PR.
 */
class EncodingUtilsTest {

    @Test
    void headerEndDelimiterIsTilde() {
        String encoded = EncodingUtils.buildEncodedString("HEADER", "BODY");
        assertEquals("HEADER~BODY", encoded);
        assertEquals("HEADER", EncodingUtils.getHeaderFromEncoding(encoded));
        assertEquals("BODY", EncodingUtils.getBodyFromEncoding(encoded));
    }

    @Test
    void emptyBodyParsesToEmptyString() {
        assertEquals("", EncodingUtils.getBodyFromEncoding("HEADERONLY"));
        assertEquals("", EncodingUtils.getBodyFromEncoding("HEADER~"));
    }

    @Test
    void delimitersAreStable() {
        // Locking the delimiter constants — these are part of the on-disk contract.
        assertEquals("|", EncodingUtils.COLUMN_END);
        assertEquals("-", EncodingUtils.ROW_END);
        assertEquals(".", EncodingUtils.DOUBLE_CHARACTER);
        assertEquals("=", EncodingUtils.MAPS_TO);
        assertEquals(":", EncodingUtils.MODIFIER);
        assertEquals("!", EncodingUtils.CONDENSED);
        assertEquals("A", EncodingUtils.VOID);
    }

    @Test
    void encodeAlphabetIsLockedAt52Symbols() {
        assertEquals(52, EncodingUtils.BLU3_ENCODE.length);
        assertEquals("A", EncodingUtils.BLU3_ENCODE[0]);
        assertEquals("Z", EncodingUtils.BLU3_ENCODE[25]);
        assertEquals("a", EncodingUtils.BLU3_ENCODE[26]);
        assertEquals("z", EncodingUtils.BLU3_ENCODE[51]);
    }

    @Test
    void ingredientsMapRoundTrip() {
        Map<String, String> input = new LinkedHashMap<>();
        input.put("STONE", "B");
        input.put("DIRT", "C");

        String encoded = EncodingUtils.ingredientsMapToString(input);
        // Format: <code>=<material>-<code>=<material>
        assertTrue(encoded.contains("B=STONE"));
        assertTrue(encoded.contains("C=DIRT"));
        assertFalse(encoded.endsWith("-"), "trailing row delimiter should be trimmed");

        Map<String, String> parsed = EncodingUtils.getIngredientsMapFromHeader(encoded + "|0:0:0");
        assertEquals("B", parsed.get("STONE"));
        assertEquals("C", parsed.get("DIRT"));
    }

    @Test
    void ingredientsMapAssignsCodesByDescendingFrequency() {
        // Most-common material gets the LOWEST code index (B = index 1, A reserved for VOID).
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("RARE", 1);
        counts.put("COMMON", 100);
        counts.put("MEDIUM", 10);

        Map<String, String> result = EncodingUtils.buildIngredientsMapFromIngredientsCount(counts);
        assertEquals("B", result.get("COMMON"));
        assertEquals("C", result.get("MEDIUM"));
        assertEquals("D", result.get("RARE"));
    }

    @Test
    void ingredientsMapSpillsToDoubleCharBeyond51Materials() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < 60; i++) {
            counts.put("MAT_" + i, 60 - i);
        }
        Map<String, String> result = EncodingUtils.buildIngredientsMapFromIngredientsCount(counts);
        // First entries are single-char.
        assertEquals("B", result.get("MAT_0"));
        // After exhausting BLU3_ENCODE (52 symbols, starting at index 1 => 51 single-char slots),
        // the encoder rolls into double-char form prefixed by ".".
        long doubleChar = result.values().stream().filter(s -> s.startsWith(".")).count();
        assertTrue(doubleChar > 0, "expected some double-char codes for >51 materials");
    }

    @Test
    void getSizesFromHeaderParsesXYZ() {
        String header = "B=STONE|3:4:5|S-0-1";
        assertArrayEquals(new int[] {3, 4, 5}, EncodingUtils.getSizesFromHeader(header));
    }

    @Test
    void getSizesFromHeaderReturnsEmptyArrayOnMalformed() {
        assertArrayEquals(new int[0], EncodingUtils.getSizesFromHeader(""));
        assertArrayEquals(new int[0], EncodingUtils.getSizesFromHeader("B=STONE"));
        assertArrayEquals(new int[0], EncodingUtils.getSizesFromHeader("B=STONE|notanumber"));
        assertArrayEquals(new int[0], EncodingUtils.getSizesFromHeader("B=STONE|1:2"));
    }

    @Test
    void directionalDataDefaultsWhenMissing() {
        // Missing perspective section -> defaults: NORTH, TOP, scale 1.
        ManipulatablePosition p = EncodingUtils.getDirectionalDataFromHeader("B=STONE|1:1:1");
        assertEquals(Orientation.NORTH, p.getOrientation());
        assertEquals(Rotation.TOP, p.getRotation());
        assertEquals(1, p.getScale());
    }

    @Test
    void directionalDataPartialMalformedFallsBackPerField() {
        // Lock the actual current behavior: a single garbage perspective token
        // resolves orientation via Orientation.getOrientation(...) -> SOUTH
        // fallback, then throws on the missing rotation/scale tokens, so
        // rotation and scale keep their initial defaults (TOP, 1).
        ManipulatablePosition p = EncodingUtils.getDirectionalDataFromHeader("B=STONE|1:1:1|garbage");
        assertEquals(Orientation.SOUTH, p.getOrientation());
        assertEquals(Rotation.TOP, p.getRotation());
        assertEquals(1, p.getScale());
    }

    @Test
    void directionalDataParsesValid() {
        // Orientation description is first letter of BlockFace name.
        // E = EAST, rotation code 1 = RIGHT, scale 3.
        ManipulatablePosition p = EncodingUtils.getDirectionalDataFromHeader("B=STONE|1:1:1|E-1-3");
        assertEquals(Orientation.EAST, p.getOrientation());
        assertEquals(Rotation.RIGHT, p.getRotation());
        assertEquals(3, p.getScale());
    }

    @Test
    void buildHeaderWithPerspectiveRoundTrip() {
        ManipulatablePosition p = new ManipulatablePosition(2, 3, 4, Orientation.WEST, Rotation.BOTTOM, 5);
        String header = EncodingUtils.buildHeaderWithPerspective("B=STONE", p);

        assertArrayEquals(new int[] {4, 3, 2}, EncodingUtils.getSizesFromHeader(header));
        ManipulatablePosition parsed = EncodingUtils.getDirectionalDataFromHeader(header);
        assertEquals(Orientation.WEST, parsed.getOrientation());
        assertEquals(Rotation.BOTTOM, parsed.getRotation());
        assertEquals(5, parsed.getScale());
    }

    @Test
    void isEncodedRecognizesAlphabetSymbols() {
        assertTrue(EncodingUtils.isEncoded("A"));
        assertTrue(EncodingUtils.isEncoded("z"));
        assertFalse(EncodingUtils.isEncoded("AA"));
        assertFalse(EncodingUtils.isEncoded("1"));
        assertFalse(EncodingUtils.isEncoded(""));
    }

    @Test
    void modifierSplitUsesColonDelimiter() {
        assertArrayEquals(new String[] {"a", "b", "c"}, EncodingUtils.modifierSplit("a:b:c"));
        assertArrayEquals(new String[] {"abc"}, EncodingUtils.modifierSplit("abc"));
    }

    /**
     * Golden fixture: a fully encoded blueprint string parses to known values.
     * If this test breaks, the on-disk encoding contract has shifted and any
     * existing exported blueprints would no longer load.
     */
    @Test
    void goldenFixtureRoundTrip() {
        String encoded = "B=STONE-C=DIRT|2:3:4|N-0-1~body";
        String header = EncodingUtils.getHeaderFromEncoding(encoded);
        String body = EncodingUtils.getBodyFromEncoding(encoded);

        assertEquals("body", body);
        assertArrayEquals(new int[] {2, 3, 4}, EncodingUtils.getSizesFromHeader(header));
        Map<String, String> ingredients = EncodingUtils.getIngredientsMapFromHeader(header);
        assertEquals("B", ingredients.get("STONE"));
        assertEquals("C", ingredients.get("DIRT"));

        ManipulatablePosition p = EncodingUtils.getDirectionalDataFromHeader(header);
        assertEquals(Orientation.NORTH, p.getOrientation());
        assertEquals(Rotation.TOP, p.getRotation());
        assertEquals(1, p.getScale());
    }
}
