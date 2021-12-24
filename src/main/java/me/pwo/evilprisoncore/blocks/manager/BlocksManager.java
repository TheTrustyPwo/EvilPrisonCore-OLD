package me.pwo.evilprisoncore.blocks.manager;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlocksManager {
    private final Blocks blocks;
    private static final List<String> blocksTopFormat = Arrays.asList(
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
            "&e&lBLOCKS TOP",
            "{FOR_EACH_PLAYER} &f&l#%position%. &e%player% &8&7%blocks% Blocks",
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"
    );
    private final HashMap<UUID, Long> blocksCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Blocks = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public BlocksManager(Blocks blocks) {
        this.blocks = blocks;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.blocks.getPlugin().getPluginDatabase().addIntoBlocks(e.getPlayer());
                    this.blocksCache.put(e.getPlayer().getUniqueId(),
                            this.blocks.getPlugin().getPluginDatabase().getPlayerBlocks(e.getPlayer()));
                })).bindWith(blocks.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerData(e.getPlayer(), true, true)).bindWith(blocks.getPlugin());
        loadPlayerDataOnEnable();
        updateBlocksTop();
    }

    public void stopUpdating() {
        this.blocks.getPlugin().getLogger().info("Stopping updating Top 10");
        this.task.close();
    }

    private void updateBlocksTop() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            Players.all().forEach((player) -> this.savePlayerData(player, false, false));
            this.top10Blocks = new LinkedHashMap<>();
            this.top10Blocks = (LinkedHashMap<UUID, Long>) this.blocks.getPlugin().getPluginDatabase().getTop10Blocks();
            this.updating = false;
        }, 30L, TimeUnit.SECONDS, 10, TimeUnit.MINUTES);
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean runAsync) {
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(player, this.blocksCache.getOrDefault(player.getUniqueId(), 0L));
                if (removeFromCache) this.blocksCache.remove(player.getUniqueId());
            });
        } else {
            this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(player, this.blocksCache.getOrDefault(player.getUniqueId(), 0L));
            if (removeFromCache) this.blocksCache.remove(player.getUniqueId());
        }
    }

    public void savePlayerDataOnDisable() {
        Schedulers.sync().run(() -> {
            for (UUID uuid : this.blocksCache.keySet())
                this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(Players.getOfflineNullable(uuid), this.blocksCache.get(uuid));
            this.blocksCache.clear();
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(this::loadPlayerData);
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> this.blocksCache.put(player.getUniqueId(), this.blocks.getPlugin().getPluginDatabase().getPlayerBlocks(player)));
    }

    public void setBlocks(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            if (player.isOnline()) this.blocksCache.put(player.getUniqueId(), amount);
            else this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(player, amount);
        });
    }

    public void giveBlocks(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long playerBlocks = getPlayerBlocks(player);
            if (player.isOnline())
                this.blocksCache.replace(player.getUniqueId(), this.blocksCache.getOrDefault(player.getUniqueId(), 0L) + amount);
            else this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(player, amount + playerBlocks);
        });
    }

    public void removeBlocks(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long newAmount = getPlayerBlocks(player) - amount;
            if (player.isOnline()) this.blocksCache.put(player.getUniqueId(), newAmount);
            else this.blocks.getPlugin().getPluginDatabase().updatePlayerBlocks(player, amount);
        });
    }

    public long getPlayerBlocks(OfflinePlayer player) {
        if (!player.isOnline()) return this.blocks.getPlugin().getPluginDatabase().getPlayerBlocks(player);
        return this.blocksCache.getOrDefault(player.getUniqueId(), 0L);
    }

    public void sendBlocksTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, "&c&lLeaderboard is currently updating...");
            return;
        }
        for (String str : blocksTopFormat) {
            if (str.startsWith("{FOR_EACH_PLAYER}")) {
                str = str.replace("{FOR_EACH_PLAYER} ", "");
                for (byte position = 0; position < 10; position++) {
                    try {
                        String player;
                        UUID uUID = (UUID)this.top10Blocks.keySet().toArray()[position];
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(uUID);
                        if (offlinePlayer == null) {
                            player = "Unknown Player";
                        } else {
                            player = offlinePlayer.getName();
                        }
                        long blocks = this.top10Blocks.get(uUID);
                        PlayerUtils.sendMessage(sender, str.replace("%position%", String.valueOf(position + 1))
                                .replace("%player%", player)
                                .replace("%blocks%", String.valueOf(blocks)));
                    } catch (Exception exception) {
                        break;
                    }
                }
                continue;
            }
            PlayerUtils.sendMessage(sender, str);
        }
    }

    public void sendInfoMessage(CommandSender sender, OfflinePlayer player) {
        Schedulers.async().run(() -> {
            if (sender == player) {
                PlayerUtils.sendMessage(sender, "&eYour Blocks: %blocks%"
                        .replace("%blocks%", String.valueOf(getPlayerBlocks(player))));
            } else {
                PlayerUtils.sendMessage(sender, "&e%player%'s Blocks: %blocks%"
                        .replace("%blocks%", String.valueOf(getPlayerBlocks(player)))
                        .replace("%player%", player.getName()));
            }
        });
    }
}
