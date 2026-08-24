package com.spearforge.sBank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

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

    @Test
    void preservesDecimalDefaultInterest() {
        assertEquals(0.5D, MiscUtils.resolveInterestRate(Stream.empty(), 0.5D));
    }

    @Test
    void acceptsDecimalInterestPermissionsAndUsesHighestRate() {
        assertEquals(1.25D, MiscUtils.resolveInterestRate(
                Stream.of("sbank.interest", "sbank.interest.0.5", "sbank.interest.1.25"), 0.5D));
    }

    @Test
    void ignoresMalformedInterestPermissions() {
        assertEquals(0.5D, MiscUtils.resolveInterestRate(
                Stream.of("sbank.interest.*", "sbank.interest.-1", "sbank.interest.free"), 0.5D));
    }
}
