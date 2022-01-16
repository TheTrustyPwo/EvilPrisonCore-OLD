package me.pwo.evilprisoncore.gems.api;

import me.pwo.evilprisoncore.gems.manager.GemsManager;
import org.bukkit.OfflinePlayer;

public class GemsAPIImpl implements GemsAPI {
    private final GemsManager manager;

    public GemsAPIImpl(GemsManager gemsManager) {
        this.manager = gemsManager;
    }

    public long getPlayerGems(OfflinePlayer player) {
        return this.manager.getPlayerGems(player);
    }

    public boolean hasEnough(OfflinePlayer player, long amount) {
        return (getPlayerGems(player) >= amount);
    }

    public void removeGems(OfflinePlayer player, long amount) { this.manager.removeGems(player, amount); }

    public void addGems(OfflinePlayer player, long amount, boolean applyMultiplier) { this.manager.giveGems(player, amount, applyMultiplier); }
}
