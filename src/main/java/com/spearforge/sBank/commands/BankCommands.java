package com.spearforge.sBank.commands;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.guis.AdminGUI;
import com.spearforge.sBank.guis.BankGUI;
import com.spearforge.sBank.guis.DebtGui;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.utils.TextUtils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class BankCommands implements CommandExecutor {

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("sbank.admin")) {
                SBank.getPlugin().reloadConfig();
                SBank.getGuiConfig().reloadConfig();
                TextUtils.sendMessageWithPrefix(sender, SBank.getPlugin().getConfig().getString("messages.plugin-reloaded"));
            } else {
                TextUtils.sendMessageWithPrefix(sender, SBank.getPlugin().getConfig().getString("messages.no-permission"));
            }
            return true;
        }

        if (sender instanceof Player){
            Player player = (Player) sender;
            if (player.hasPermission("sbank.use") || player.hasPermission("sbank.admin")){
                if (args.length == 0){
                    if (SBank.getPlugin().getConfig().getBoolean("npc-bankers.enabled") && !player.hasPermission(SBank.getPlugin().getConfig().getString("npc-bankers.bypass-permission"))){
                        TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("npc-bankers.command-message"));
                    } else {
                        Inventory inventory = BankGUI.getBankHomePage((Player) sender);
                        player.openInventory(inventory);
                    }
                } else if (args.length == 1){
                    if (args[0].equalsIgnoreCase("debt")){
                        if (SBank.getPlugin().getConfig().getBoolean("npc-bankers.enabled") && !player.hasPermission(SBank.getPlugin().getConfig().getString("npc-bankers.bypass-permission"))){
                            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("npc-bankers.command-message"));
                        } else {
                            if (player.hasPermission("sbank.loan")) {
                                if (SBank.getPlugin().getConfig().getBoolean("loan.enabled")) {
                                    player.openInventory(DebtGui.openDebtPage(player));
                                } else {
                                    TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.loan-disabled"));
                                }
                            } else {
                                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
                            }
                        }
                    } else if (Bukkit.getOfflinePlayer(args[0]) != null){ // check if player has a bank
                        if (player.hasPermission("sbank.admin")) {
                            Bank pBank = SBank.getDb().getBank(args[0]);
                            if (pBank != null){
                                player.openInventory(AdminGUI.openPlayerBankGUI(pBank));
                            } else {
                                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.player-not-found"));
                            }
                        } else {
                            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
                        }
                    }
                }
            } else {
                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
            }
        }


        return true;
    }

}
