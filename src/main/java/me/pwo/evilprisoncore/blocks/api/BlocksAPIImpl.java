package me.pwo.evilprisoncore.blocks.api;

import me.pwo.evilprisoncore.blocks.manager.BlocksManager;
import org.bukkit.OfflinePlayer;

public class BlocksAPIImpl implements BlocksAPI {
    private final BlocksManager manager;

    public BlocksAPIImpl(BlocksManager tokensManager) {
        this.manager = tokensManager;
    }

    public long getPlayerBlocks(OfflinePlayer player) {
        return this.manager.getPlayerBlocks(player);
    }

    public boolean hasEnough(OfflinePlayer player, long amount) {
        return (getPlayerBlocks(player) >= amount);
    }

    public void removeBlocks(OfflinePlayer player, long amount) { this.manager.removeBlocks(player, amount); }

    public void addBlocks(OfflinePlayer player, long amount) { this.manager.giveBlocks(player, amount); }
}
