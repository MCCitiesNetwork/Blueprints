package io.github.bl3rune.blueprints.services.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Phase 8 coverage for the size/scale arithmetic split out of {@code BlueprintData}.
 * The permission-bearing methods need a {@code Player}; only the pure
 * arithmetic predicate is exercised here.
 */
class LimitValidatorTest {

    private final LimitValidator validator = new LimitValidator();

    @Test
    void allDimensionsUnderLimitAtScaleOne() {
        assertFalse(validator.sizesExceedLimit(new int[] { 5, 5, 5 }, 1, 10));
    }

    @Test
    void singleDimensionOverLimitAtScaleOne() {
        assertTrue(validator.sizesExceedLimit(new int[] { 5, 11, 5 }, 1, 10));
    }

    @Test
    void scaleAmplifiesProductPastLimit() {
        // 5 * 3 = 15 > 10
        assertTrue(validator.sizesExceedLimit(new int[] { 5, 5, 5 }, 3, 10));
    }

    @Test
    void exactlyAtLimitDoesNotExceed() {
        // strict greater-than is the contract preserved from the original
        // BlueprintData implementation
        assertFalse(validator.sizesExceedLimit(new int[] { 10, 10, 10 }, 1, 10));
    }

    @Test
    void zeroLengthArrayNeverExceeds() {
        assertFalse(validator.sizesExceedLimit(new int[0], 100, 1));
    }

    @Test
    void firstOverLimitReturnsTrueWithoutCheckingRest() {
        // not directly observable, but the contract is short-circuit on first hit
        assertTrue(validator.sizesExceedLimit(new int[] { 100, 1, 1 }, 1, 10));
        assertTrue(validator.sizesExceedLimit(new int[] { 1, 1, 100 }, 1, 10));
    }
}
