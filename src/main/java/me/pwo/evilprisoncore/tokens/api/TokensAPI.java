package me.pwo.evilprisoncore.tokens.api;

import org.bukkit.OfflinePlayer;

public interface TokensAPI {
    long getPlayerTokens(OfflinePlayer player);

    boolean hasEnough(OfflinePlayer player, long amount);

    void removeTokens(OfflinePlayer player, long amount);

    void addTokens(OfflinePlayer player, long amount, boolean applyMultiplier);
}
