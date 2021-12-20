package me.pwo.evilprisoncore.ranks.manager;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.ranks.model.RankModel;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RankManager {
    private final Ranks ranks;
    private Map<UUID, Long> onlinePlayersRanks = new HashMap<>();
    private List<String> rankTopFormat;
    private boolean updating;
    private double unlimitedRankCost;
    private double increaseCostBy;
    private Map<Long, RankModel> rankCheckpoints;
    private LinkedHashMap<UUID, Long> top10Ranks;
    private Task task;
    private int rankTopUpdateInterval;
    private boolean unlimitedRanksRewardPerRankEnabled;
    private List<String> unlimitedRanksRewardPerRank;
    private Map<Range<Long>, String> prefix;

    public RankManager(Ranks ranks) {
        this.ranks = ranks;
        reload();
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.ranks.getPlugin().getPluginDatabase().addIntoRanks(e.getPlayer());
                    this.onlinePlayersRanks.put(e.getPlayer().getUniqueId(), this.ranks.getPlugin().getPluginDatabase().getPlayerRank(e.getPlayer()));
                })).bindWith(ranks.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerRank(e.getPlayer()))
                .bindWith(ranks.getPlugin());
        updateTop10();
    }

    public void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersRanks.keySet())
            this.ranks.getPlugin().getPluginDatabase().updatePlayerRank(Players.getOfflineNullable(uuid), this.onlinePlayersRanks.get(uuid));
        this.ranks.getPlugin().getLogger().info("Saved players ranks!");
    }

    private void savePlayerRank(Player player) {
        Schedulers.async().run(() -> {
            this.ranks.getPlugin().getPluginDatabase().updatePlayerRank(player, getPlayerRank(player).getId());
            this.onlinePlayersRanks.remove(player.getUniqueId());
            this.ranks.getPlugin().getLogger().info("Saved " + player.getName() + "'s rank to database.");
        });
    }

    public void reload() {
        this.rankTopFormat = this.ranks.getConfig().getStringList("rank-top-format");
        this.unlimitedRankCost = this.ranks.getConfig().getDouble("unlimited_ranks.rank_cost");
        this.increaseCostBy = this.ranks.getConfig().getDouble("unlimited_ranks.increase_cost.increase_cost_by");
        this.unlimitedRanksRewardPerRankEnabled = this.ranks.getConfig().getBoolean("unlimited_ranks.rewards-per-rank.enabled");
        if (this.unlimitedRanksRewardPerRankEnabled)
            this.unlimitedRanksRewardPerRank = this.ranks.getConfig().getStringList("unlimited_ranks.rewards-per-rank.rewards");
        this.rankTopUpdateInterval = this.ranks.getConfig().getInt("rank_top_update_interval");
        loadRankCheckpoints();
        loadPrefixes();
    }

    private void loadPrefixes() {
        this.prefix = new HashMap<>();
        ConfigurationSection configurationSection = this.ranks.getConfig().getConfigurationSection("prefix");
        for (String string : configurationSection.getKeys(false)) {
            if (string.equalsIgnoreCase("others")) {
                continue;
            }
            String[] strings = string.split("-");
            prefix.put(Range.between(Long.parseLong(strings[0]), Long.parseLong(strings[1])),
                    configurationSection.getString(string));
        }
    }

    public String getPrefix(long rank) {
        for (Range<Long> range : this.prefix.keySet()) {
            if (range.contains(rank)) {
                return this.prefix.get(range).replaceAll("%rank%", String.valueOf(rank));
            }
        }
        return this.ranks.getConfig().getString("prefix.others").replaceAll("%rank%", String.valueOf(rank));
    }

    public int getMineSize(long rank) {
        if (rankCheckpoints.containsKey(rank)) {
            return rankCheckpoints.get(rank).getMineSize();
        }
        Iterator<Long> iterator = rankCheckpoints.keySet().iterator();
        while (iterator.hasNext()) {
            long l1 = iterator.next();
            long l2 = iterator.hasNext() ? iterator.next() : l1;
            if (l1 < rank || rank < l2) {
                return rankCheckpoints.get(l1).getMineSize();
            }
        }
        return 9;
    }

    public List<Material> getUnlockedBlocks(long rank) {
        List<Material> list = new ArrayList<>();
        for (long l : rankCheckpoints.keySet()) {
            if (l <= rank) {
                list.addAll(rankCheckpoints.get(l).getUnlockedBlocks());
            } else {
                break;
            }
        }
        return list;
    }

    private void loadRankCheckpoints() {
        this.rankCheckpoints = new LinkedHashMap<>();
        ConfigurationSection configurationSection = this.ranks.getConfig().getConfigurationSection("unlimited_ranks.rewards");
        for (String str : configurationSection.getKeys(false)) {
            try {
                long rank = Long.parseLong(str);
                List<Material> list = new ArrayList<>();
                configurationSection.getStringList(str + ".unlocked_blocks").forEach((s) -> list.add(Material.getMaterial(s)));
                this.rankCheckpoints.put(rank, new RankModel(
                        rank,
                        calculateRankCost(rank),
                        getPrefix(rank),
                        configurationSection.getStringList(str + ".CMD"),
                        configurationSection.getInt(str + ".mine_size"),
                        list
                ));
            } catch (Exception ignored) {}
        }
    }

    public RankModel getNextRank(RankModel rankModel) {
        return getRank(rankModel.getId() + 1);
    }

    private RankModel getRank(long rank) {
        return this.rankCheckpoints.getOrDefault(rank, new RankModel(
                rank,
                calculateRankCost(rank),
                getPrefix(rank),
                null,
                getMineSize(rank),
                null
        ));
    }

    public RankModel getPlayerRank(OfflinePlayer player) {
        long rank = player.isOnline() ?
                this.onlinePlayersRanks.getOrDefault(player.getUniqueId(), 0L) :
                this.ranks.getPlugin().getPluginDatabase().getPlayerRank(player);
        return getRank(rank);
    }

    private double calculateRankCost(long rank) {
        double d = this.unlimitedRankCost;
        long l;
        for (l = 0L; l < rank; l++) {
            if (l != 0L)
                d *= this.increaseCostBy;
        }
        return d;
    }

    private boolean completeTransaction(Player player, double amount) {
        return this.ranks.getPlugin().getEconomy().withdrawPlayer(player, amount).transactionSuccess();
    }

    private boolean isTransactionAllowed(Player player, double amount) {
        return this.ranks.getPlugin().getEconomy().has(player, amount);
    }

    public void buyNextRank(Player player) {
        RankModel currentRank = getPlayerRank(player);
        RankModel nextRank = getNextRank(currentRank);
        if (!isTransactionAllowed(player, nextRank.getCost())) {
            PlayerUtils.sendMessage(player, this.ranks.getMessage("not_enough_money_rankup")
                    .replace("%cost%", String.format("%,.0f", nextRank.getCost())));
            return;
        }
        doRankUp(player, nextRank);
        String[] message = this.ranks.getMessage("rankup_title")
                .replace("%rank%", String.valueOf(nextRank.getId()))
                .split("%split%");
        PlayerUtils.sendTitle(player, message[0], message[1], 10, 50, 10);
        PlayerUtils.sendMessage(player, this.ranks.getMessage("rankup_message")
                .replace("%rank%", String.valueOf(String.format("%,d", nextRank.getId()))));
    }

    public void sendRankTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, this.ranks.getMessage("top_updating"));
            return;
        }
        for (String str : this.rankTopFormat) {
            if (str.startsWith("{FOR_EACH_PLAYER}")) {
                String str1 = str.replace("{FOR_EACH_PLAYER} ", "");
                for (byte b = 0; b < 10; b++) {
                    try {
                        String str2;
                        UUID uUID = (UUID)this.top10Ranks.keySet().toArray()[b];
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(uUID);
                        if (offlinePlayer == null) {
                            str2 = "Unknown Player";
                        } else {
                            str2 = offlinePlayer.getName();
                        }
                        long l = this.top10Ranks.get(uUID);
                        PlayerUtils.sendMessage(sender, str1
                                .replace("%position%", String.valueOf(b + 1))
                                .replace("%player%", str2)
                                .replace("%rank%", String.format("%,d", l)));
                    } catch (Exception exception) {
                        break;
                    }
                }
                continue;
            }
            PlayerUtils.sendMessage(sender, str);
        }
    }

    private void updateTop10() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            saveAllDataSync();
            this.top10Ranks = new LinkedHashMap<>();
            this.ranks.getPlugin().getLogger().info("Starting updating RankTop");
            this.top10Ranks = (LinkedHashMap<UUID, Long>)this.ranks.getPlugin().getPluginDatabase().getTop10Ranks();
            this.ranks.getPlugin().getLogger().info("RankTop updated!");
            this.updating = false;
        }, 30L, TimeUnit.SECONDS, this.rankTopUpdateInterval, TimeUnit.MINUTES);
    }

    public void stopUpdating() {
        this.ranks.getPlugin().getLogger().info("Stopping updating Top 10 - Ranks");
        this.task.close();
    }

    public void buyMaxRank(Player player) {
        RankModel currentRank = getPlayerRank(player);
        RankModel nextRank = getNextRank(currentRank);
        if (!isTransactionAllowed(player, nextRank.getCost())) {
            PlayerUtils.sendMessage(player, this.ranks.getMessage("not_enough_money_rankup")
                    .replace("%cost%", String.format("%,.0f", nextRank.getCost())));
            return;
        }
        while (player.isOnline() && isTransactionAllowed(player, nextRank.getCost())) {
            doRankUp(player, nextRank);
            nextRank = getNextRank(nextRank);
        }
        if (currentRank.getId() < this.onlinePlayersRanks.get(player.getUniqueId())) {
            String[] message = this.ranks.getMessage("max_rankup_done_title")
                    .replace("%rank%", String.format("%,d", this.onlinePlayersRanks.get(player.getUniqueId())))
                    .split("%split%");
            PlayerUtils.sendTitle(player, message[0], message[1], 10, 50, 10);
            PlayerUtils.sendMessage(player, this.ranks.getMessage("max_rankup_done_message")
                    .replace("%start_rank%", String.format("%,d", currentRank.getId()))
                    .replace("%rank%", String.format("%,d", this.onlinePlayersRanks.get(player.getUniqueId()))));
        }
    }

    private void doRankUp(Player player, RankModel rankModel) {
        if (!completeTransaction(player, rankModel.getCost()))
            return;
        this.onlinePlayersRanks.put(player.getUniqueId(), rankModel.getId());
        rankModel.runCommands(player);
        if (this.unlimitedRanksRewardPerRank != null)
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> this.unlimitedRanksRewardPerRank.forEach((paramString -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), paramString.replace("%player%", player.getName())))));
            } else {
                this.unlimitedRanksRewardPerRank.forEach(paramString -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), paramString.replace("%player%", player.getName())));
            }
    }

    public void setRank(Player paramPlayer, RankModel paramRank, CommandSender paramCommandSender) {
        paramRank.runCommands(paramPlayer);
        this.onlinePlayersRanks.put(paramPlayer.getUniqueId(), paramRank.getId());
        if (paramCommandSender != null) {
            PlayerUtils.sendMessage(paramCommandSender, this.ranks.getMessage("rank_set")
                    .replace("%rank%", paramRank.getPrefix())
                    .replace("%player%", paramPlayer.getName()));
        }
    }

    public int getRankUpProgress(Player paramPlayer) {
        RankModel currentRank = getPlayerRank(paramPlayer);
        RankModel nextRank = getNextRank(currentRank);
        double d = this.ranks.getPlugin().getEconomy().getBalance(paramPlayer);
        int i = (int)(d / nextRank.getCost() * 100.0D);
        if (i > 100)
            i = 100;
        return i;
    }

    public double getNextRankCost(Player paramPlayer) {
        return getNextRank(getPlayerRank(paramPlayer)).getCost();
    }
}
