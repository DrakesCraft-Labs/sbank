package com.spearforge.sBank.modules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WealthTaxModuleTest {

    private static final List<WealthTaxModule.Tier> TIERS = List.of(
            new WealthTaxModule.Tier(1_000_000D, 0.5D),
            new WealthTaxModule.Tier(5_000_000D, 1D),
            new WealthTaxModule.Tier(-1D, 2D));

    @Test
    void protectsBalancesAtOrBelowTheMinimum() {
        assertEquals(0D, WealthTaxModule.calculate(500_000D, 500_000D, 250_000D, TIERS));
    }

    @Test
    void taxesOnlyTheWealthAboveTheMinimum() {
        assertEquals(2_495D, WealthTaxModule.calculate(999_000D, 500_000D, 250_000D, TIERS));
    }

    @Test
    void appliesProgressiveBracketsAndTheCycleCap() {
        assertEquals(55_000D, WealthTaxModule.calculate(6_000_000D, 500_000D, 250_000D, TIERS));
        assertEquals(250_000D, WealthTaxModule.calculate(100_000_000D, 500_000D, 250_000D, TIERS));
    }
}
