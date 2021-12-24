package me.pwo.evilprisoncore.blocks.api;

import org.bukkit.OfflinePlayer;

public interface BlocksAPI {
    long getPlayerBlocks(OfflinePlayer player);

    boolean hasEnough(OfflinePlayer player, long amount);

    void removeBlocks(OfflinePlayer player, long amount);

    void addBlocks(OfflinePlayer player, long amount);
}
