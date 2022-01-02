package me.pwo.evilprisoncore.autosell.api;

import me.pwo.evilprisoncore.autosell.AutoSell;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AutoSellAPIImpl implements AutoSellAPI {
    private final AutoSell autoSell;

    public AutoSellAPIImpl(AutoSell autoSell) {
        this.autoSell = autoSell;
    }

    public double getCurrentEarnings(Player paramPlayer) {
        return this.autoSell.getCurrentEarnings(paramPlayer);
    }

    public double getPriceForBlock(Material material) {
        return this.autoSell.getPriceForBrokenBlock(material);
    }

    public boolean hasAutoSellEnabled(Player paramPlayer) {
        return this.autoSell.hasAutoSellEnabled(paramPlayer);
    }
}
