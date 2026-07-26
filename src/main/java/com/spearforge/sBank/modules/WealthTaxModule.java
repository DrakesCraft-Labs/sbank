package com.spearforge.sBank.modules;

import java.util.List;

/** Calculates a capped progressive tax over banked wealth only. */
public final class WealthTaxModule {

    private WealthTaxModule() {
    }

    public record Tier(double upperBound, double ratePercent) {
    }

    public static double calculate(double balance, double protectedBalance, double maximumCharge, List<Tier> tiers) {
        if (!Double.isFinite(balance) || balance <= protectedBalance || maximumCharge <= 0) {
            return 0;
        }

        double taxable = balance - Math.max(0, protectedBalance);
        double previousUpperBound = 0;
        double tax = 0;

        for (Tier tier : tiers) {
            if (taxable <= previousUpperBound) {
                break;
            }

            double tierUpperBound = tier.upperBound() <= 0 ? taxable : tier.upperBound();
            double taxableAtTier = Math.max(0, Math.min(taxable, tierUpperBound) - previousUpperBound);
            tax += taxableAtTier * Math.max(0, tier.ratePercent()) / 100.0D;
            previousUpperBound = tierUpperBound;
        }

        return Math.round(Math.min(tax, maximumCharge) * 100.0D) / 100.0D;
    }
}
