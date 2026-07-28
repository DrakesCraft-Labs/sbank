package com.spearforge.sBank.modules;

import com.spearforge.sBank.SBank;

/**
 * Persistent reserve that makes new loans depend on money already removed from circulation.
 * All mutations are synchronized because scheduled tax cycles and player actions share it.
 */
public final class EconomyReserve {

    private static final String BALANCE_PATH = "economy-reserve.balance";

    private EconomyReserve() {
    }

    public static synchronized boolean allocateLoan(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            return false;
        }
        if (!SBank.getPlugin().getConfig().getBoolean("economy-reserve.reserve-backed-loans", true)) {
            return true;
        }
        double available = balance();
        if (available + 0.0001D < amount) {
            return false;
        }
        setBalance(available - amount);
        return true;
    }

    public static synchronized void credit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            return;
        }
        setBalance(balance() + amount);
    }

    public static synchronized void restoreLoan(double amount) {
        credit(amount);
    }

    public static synchronized double balance() {
        return Math.max(0D, SBank.getPlugin().getConfig().getDouble(BALANCE_PATH, 0D));
    }

    private static void setBalance(double value) {
        double rounded = Math.round(Math.max(0D, value) * 100D) / 100D;
        SBank.getPlugin().getConfig().set(BALANCE_PATH, rounded);
        SBank.getPlugin().saveConfig();
    }
}
