package com.spearforge.sBank.modules;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.listener.DebtGuiListener;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebtModule {

    public static boolean hasDefinedHoursPassed(String username) {
        try {
            String lastDebtPayment = SBank.getDebts().get(username).getLastPaymentDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime lastDebtDate = LocalDateTime.parse(lastDebtPayment, formatter);

            LocalDateTime currentTime = LocalDateTime.now();

            Duration duration = Duration.between(lastDebtDate, currentTime);

            long hoursPassed = duration.toHours();
            return hoursPassed >= Long.parseLong(SBank.getPlugin().getConfig().getString("loan.debt-time"));
        } catch (Exception e) {
            SBank.getPlugin().getLogger().warning("An error occurred while checking if the debt time has passed");
            return false;
        }
    }

    public static void updateLastPaymentDate(String username) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateFormatted = dateTime.format(formatter);

        SBank.getDebts().get(username).setLastPaymentDate(dateFormatted);
    }

    public static String getTimeUntilNextPayment(String username) {
        try {
            String lastDebtPayment = SBank.getDebts().get(username).getLastPaymentDate();
            long debtIntervalHours = Long.parseLong(SBank.getPlugin().getConfig().getString("loan.debt-time"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime lastDebtDate = LocalDateTime.parse(lastDebtPayment, formatter);

            LocalDateTime nextDebtDate = lastDebtDate.plusHours(debtIntervalHours);
            LocalDateTime currentTime = LocalDateTime.now();

            Duration duration = Duration.between(currentTime, nextDebtDate);

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            return String.format("%02d hours, %02d minutes, %02d seconds", hours, minutes, seconds);
        } catch (Exception e) {
            return "Error calculating time until next debt";
        }
    }



    public static void payDebt(Player player, Double amount) {
        double bankBefore = SBank.getBanks().get(player.getName()).getBalance();
        double debtBefore = SBank.getDebts().get(player.getName()).getRemaining();
        double payment = Math.min(amount, debtBefore);
        SBank.getDebts().get(player.getName()).setRemaining(debtBefore - payment);
        SBank.getBanks().get(player.getName()).setBalance(bankBefore - payment);
        double wallet = SBank.getEcon().getBalance(player);
        SBank.getAuditLogger().record("DEBT_AUTO_PAYMENT", player.getName(), player.getUniqueId().toString(), payment,
                wallet, wallet, bankBefore, SBank.getBanks().get(player.getName()).getBalance(), "remaining-debt=" + (debtBefore - payment));
        EconomyReserve.credit(payment);

        if (SBank.getDebts().get(player.getName()).getRemaining() <= 0){
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.debt-paid").replaceAll("%money%", MiscUtils.formatBalance(SBank.getDebts().get(player.getName()).getTotal())));
            SBank.getDebts().remove(player.getName());
            try {
                SBank.getDb().removeDebt(player.getName());
            } catch (SQLException ex) {
                SBank.getPlugin().getLogger().warning("An error occurred while removing the debt from the database");
            }
        }else{
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.debt-payment-success").replaceAll("%money%", MiscUtils.formatBalance(amount)));
            DebtModule.updateLastPaymentDate(player.getName());
        }
    }

    public static void payDebtFromBalance(Player player, Double amount){
        double debtBefore = SBank.getDebts().get(player.getName()).getRemaining();
        double payment = Math.min(amount, debtBefore);
        double walletBefore = SBank.getEcon().getBalance(player);
        EconomyResponse response = SBank.getEcon().withdrawPlayer(player, payment);
        if (!response.transactionSuccess()) {
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString(
                    "messages.transaction-failed", "&cThe transaction could not be completed."));
            return;
        }
        SBank.getDebts().get(player.getName()).setRemaining(debtBefore - payment);
        DebtGuiListener.getDebtPayment().remove(player.getName());
        double bank = SBank.getBanks().get(player.getName()).getBalance();
        SBank.getAuditLogger().record("DEBT_WALLET_PAYMENT", player.getName(), player.getUniqueId().toString(), payment,
                walletBefore, SBank.getEcon().getBalance(player), bank, bank, "remaining-debt=" + (debtBefore - payment));
        EconomyReserve.credit(payment);

        if (SBank.getDebts().get(player.getName()).getRemaining() <= 0){
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.debt-paid").replaceAll("%money%", MiscUtils.formatBalance(SBank.getDebts().get(player.getName()).getTotal())));
            SBank.getDebts().remove(player.getName());
            try {
                SBank.getDb().removeDebt(player.getName());
            } catch (SQLException ex) {
                SBank.getPlugin().getLogger().warning("An error occurred while removing the debt from the database for " + player.getName());
            }
        }else{
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.debt-payment-success").replaceAll("%money%", MiscUtils.formatBalance(amount)));
            DebtModule.updateLastPaymentDate(player.getName());
        }

    }

}
