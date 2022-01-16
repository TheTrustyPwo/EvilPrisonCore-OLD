package me.pwo.evilprisoncore.credits.api;

import me.pwo.evilprisoncore.credits.manager.CreditsManager;
import org.bukkit.OfflinePlayer;

public class CreditsAPIImpl implements CreditsAPI {
    private final CreditsManager manager;

    public CreditsAPIImpl(CreditsManager tokensManager) {
        this.manager = tokensManager;
    }

    public long getPlayerCredits(OfflinePlayer player) {
        return this.manager.getPlayerCredits(player);
    }

    public boolean hasEnough(OfflinePlayer player, long amount) {
        return (getPlayerCredits(player) >= amount);
    }

    public void removeCredits(OfflinePlayer player, long amount) { this.manager.removeCredits(player, amount); }

    public void addCredits(OfflinePlayer player, long amount) { this.manager.giveCredits(player, amount); }
}
