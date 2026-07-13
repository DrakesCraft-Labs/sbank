package com.spearforge.sBank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import com.spearforge.sBank.utils.MoneyMath;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyPile {

    private double amount;
    private String player_name;

    public void setAmount(double amount) {
        this.amount = MoneyMath.normalize(amount);
    }

}
