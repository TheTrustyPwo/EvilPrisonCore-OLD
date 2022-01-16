package me.pwo.evilprisoncore.credits.api;

import org.bukkit.OfflinePlayer;

public interface CreditsAPI {
    long getPlayerCredits(OfflinePlayer player);

    boolean hasEnough(OfflinePlayer player, long amount);

    void removeCredits(OfflinePlayer player, long amount);

    void addCredits(OfflinePlayer player, long amount);
}
