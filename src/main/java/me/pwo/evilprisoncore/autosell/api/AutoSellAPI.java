package me.pwo.evilprisoncore.autosell.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface AutoSellAPI {
    double getCurrentEarnings(Player paramPlayer);

    double getPriceForBlock(Material material);

    boolean hasAutoSellEnabled(Player paramPlayer);
}
