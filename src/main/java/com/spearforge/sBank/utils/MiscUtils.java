package com.spearforge.sBank.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.MoneyPile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {
    private static final String PHYSICAL_MONEY_MARKER = "sbank_physical_money_v1";
    
    public static double getInterest(Player player, double _interest) {
        Pattern pattern = Pattern.compile("^sbank\\.interest\\.(\\d+)$"); // sbank.interest.<percent>

        return player.getEffectivePermissions().stream()
            .map(PermissionAttachmentInfo::getPermission)
            .map(pattern::matcher)
            .filter(Matcher::matches) 
            .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
            .max()
            .orElse((int) _interest);
    }


    public static String formatBalance(double balance) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        return df.format(balance);
    }



    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            double value = Double.parseDouble(str);
            return Double.isFinite(value) && value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static ItemStack getCustomHead(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if (base64 == null || base64.isEmpty()) {
            return head; //
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta == null) return head;

        // Paper maintains this profile API across Minecraft mappings; no reflective NMS access.
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
        headMeta.setPlayerProfile(profile);

        head.setItemMeta(headMeta);
        return head;
    }

    public static ItemStack getMaterialOrHead(String configPath){
        try {
            return new ItemStack(Material.valueOf(SBank.getGuiConfig().getString(configPath + ".material")));
        } catch (IllegalArgumentException e) {
            return getCustomHead(SBank.getGuiConfig().getString(configPath + ".material"));
        }
    }

    public static ItemStack getPhysicalMoney(Player player, double amount) {
        ItemStack physicalMoney;
        String materialName = SBank.getPlugin().getConfig().getString("physical-money.item.material");

        try {
            Material material = Material.valueOf(materialName);
            physicalMoney = new ItemStack(material);
        } catch (IllegalArgumentException e) {
            physicalMoney = getCustomHead(materialName);
        }

        MoneyPile moneyPile = new MoneyPile(amount, player.getName());

        ItemMeta meta = physicalMoney.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("physical-money.item.name")));
            meta.setLore(TextUtils.replacePlaceholders(SBank.getPlugin().getConfig().getStringList("physical-money.item.lore"), null, null, moneyPile));
            meta.getPersistentDataContainer().set(physicalMoneyKey(), PersistentDataType.DOUBLE, amount);
            meta.getPersistentDataContainer().set(physicalMoneyMarkerKey(), PersistentDataType.STRING, PHYSICAL_MONEY_MARKER);
            physicalMoney.setItemMeta(meta);
        }

        return physicalMoney;
    }

    /** Validates money created by sBank itself; display name and lore are never trusted as currency authority. */
    public static double getVerifiedPhysicalMoney(ItemMeta meta) {
        if (meta == null || !PHYSICAL_MONEY_MARKER.equals(meta.getPersistentDataContainer()
                .get(physicalMoneyMarkerKey(), PersistentDataType.STRING))) {
            return -1.0D;
        }
        Double amount = meta.getPersistentDataContainer().get(physicalMoneyKey(), PersistentDataType.DOUBLE);
        return amount != null && Double.isFinite(amount) && amount > 0.0D ? amount : -1.0D;
    }

    private static NamespacedKey physicalMoneyKey() {
        return new NamespacedKey(SBank.getPlugin(), "physical_money_amount");
    }

    private static NamespacedKey physicalMoneyMarkerKey() {
        return new NamespacedKey(SBank.getPlugin(), "physical_money_marker");
    }

    public static double extractMoney(List<String> lore) {
        Pattern pattern = Pattern.compile("\\d{1,3}(?:\\.\\d{3})*(?:,\\d+)?\\$");

        for (String line : lore) {

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String rawNumber = matcher.group();

                try {
                    String processedNumber = rawNumber.replace(".", "").replace(",", ".").replace("$", "");

                    double amount = Double.parseDouble(processedNumber);
                    return amount;
                } catch (NumberFormatException e) {
                    return -100.0;
                }
            }
        }

        return -100.0;
    }


}
