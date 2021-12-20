package me.pwo.evilprisoncore.gems.api;

import org.bukkit.OfflinePlayer;

public interface GemsAPI {
    long getPlayerGems(OfflinePlayer player);

    boolean hasEnough(OfflinePlayer player, long amount);

    void removeGems(OfflinePlayer player, long amount);

    void addGems(OfflinePlayer player, long amount, boolean applyMultiplier);
}
