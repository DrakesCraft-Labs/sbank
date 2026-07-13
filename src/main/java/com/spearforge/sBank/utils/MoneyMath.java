package com.spearforge.sBank.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Keeps persisted Vault-facing values at the two-decimal precision supported by
 * the bank UI without relying on locale-sensitive formatting.
 */
public final class MoneyMath {

    private static final int SCALE = 2;

    private MoneyMath() {
    }

    public static double normalize(double amount) {
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Money amount must be finite");
        }

        return BigDecimal.valueOf(amount)
            .setScale(SCALE, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
