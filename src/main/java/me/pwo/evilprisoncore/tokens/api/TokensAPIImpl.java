package me.pwo.evilprisoncore.tokens.api;

import me.pwo.evilprisoncore.tokens.manager.TokensManager;
import org.bukkit.OfflinePlayer;

public class TokensAPIImpl implements TokensAPI {
    private final TokensManager manager;

    public TokensAPIImpl(TokensManager tokensManager) {
        this.manager = tokensManager;
    }

    public long getPlayerTokens(OfflinePlayer player) {
        return this.manager.getPlayerTokens(player);
    }

    public boolean hasEnough(OfflinePlayer player, long amount) {
        return (getPlayerTokens(player) >= amount);
    }

    public void removeTokens(OfflinePlayer player, long amount) { this.manager.removeTokens(player, amount); }

    public void addTokens(OfflinePlayer player, long amount, boolean applyMultiplier) { this.manager.giveTokens(player, amount, applyMultiplier); }
}
