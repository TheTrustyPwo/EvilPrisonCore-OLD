package me.pwo.evilprisoncore.blocks.blockrewards.manager;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.blocks.blockrewards.BlockRewards;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class BlockRewardsManager {
    private final BlockRewards blockRewards;
    private final HashMap<UUID, Integer> blockRewardTierCache = new HashMap<>();

    public BlockRewardsManager(BlockRewards blockRewards) {
        this.blockRewards = blockRewards;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.blockRewards.getBlocks().getPlugin().getPluginDatabase().addIntoBlocks(e.getPlayer());
                    this.blockRewardTierCache.put(e.getPlayer().getUniqueId(),
                            this.blockRewards.getBlocks().getPlugin().getPluginDatabase().getPlayerBlockTier(e.getPlayer()));
                })).bindWith(blockRewards.getBlocks().getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerData(e.getPlayer(), true, true)).bindWith(this.blockRewards.getBlocks().getPlugin());
        loadPlayerDataOnEnable();
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean runAsync) {
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.blockRewards.getBlocks().getPlugin().getPluginDatabase().updatePlayerBlocks(player, this.blockRewardTierCache.getOrDefault(player.getUniqueId(), 0));
                if (removeFromCache) this.blockRewardTierCache.remove(player.getUniqueId());
            });
        } else {
            this.blockRewards.getBlocks().getPlugin().getPluginDatabase().updatePlayerBlocks(player, this.blockRewardTierCache.getOrDefault(player.getUniqueId(), 0));
            if (removeFromCache) this.blockRewardTierCache.remove(player.getUniqueId());
        }
    }

    public void savePlayerDataOnDisable() {
        Schedulers.sync().run(() -> {
            for (UUID uuid : this.blockRewardTierCache.keySet())
                this.blockRewards.getBlocks().getPlugin().getPluginDatabase().updatePlayerBlocks(Players.getOfflineNullable(uuid), this.blockRewardTierCache.get(uuid));
            this.blockRewardTierCache.clear();
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(this::loadPlayerData);
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> this.blockRewardTierCache.put(player.getUniqueId(), this.blockRewards.getBlocks().getPlugin().getPluginDatabase().getPlayerBlockTier(player)));
    }

    public void setBlockRewardTier(OfflinePlayer player, int amount) {
        Schedulers.async().run(() -> {
            if (player.isOnline()) this.blockRewardTierCache.put(player.getUniqueId(), amount);
            else this.blockRewards.getBlocks().getPlugin().getPluginDatabase().updatePlayerBlocks(player, amount);
        });
    }

    public int getPlayerBlockRewardTier(OfflinePlayer player) {
        if (!player.isOnline()) return this.blockRewards.getBlocks().getPlugin().getPluginDatabase().getPlayerBlockTier(player);
        return this.blockRewardTierCache.getOrDefault(player.getUniqueId(), 0);
    }
}
