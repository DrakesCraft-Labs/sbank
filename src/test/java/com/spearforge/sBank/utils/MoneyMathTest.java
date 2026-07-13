package com.spearforge.sBank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyMathTest {

    @Test
    void roundsUsingFinancialHalfUpPrecision() {
        assertEquals(12.35D, MoneyMath.normalize(12.345D));
        assertEquals(12.34D, MoneyMath.normalize(12.344D));
    }

    @Test
    void rejectsNonFiniteAmounts() {
        assertThrows(IllegalArgumentException.class, () -> MoneyMath.normalize(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> MoneyMath.normalize(Double.POSITIVE_INFINITY));
    }
}
