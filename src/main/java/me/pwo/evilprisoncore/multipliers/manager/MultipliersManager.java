package me.pwo.evilprisoncore.multipliers.manager;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MultipliersManager {
    private final Multipliers multipliers;
    private GlobalMultiplier globalSellMultiplier;
    private GlobalMultiplier globalTokenMultiplier;
    private HashMap<UUID, RankMultiplier> rankMultipliers = new HashMap<>();
    private HashMap<UUID, PlayerMultiplier> playerMultipliers = new HashMap<>();
    private LinkedHashMap<String, RankMultiplier> permissionToMultiplier = new LinkedHashMap<>();
    private Task rankUpdateTask;

    public MultipliersManager(Multipliers multipliers) {
        this.multipliers = multipliers;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    this.rankMultipliers.put(e.getPlayer().getUniqueId(), calculateRankMultiplier(e.getPlayer()));
                    loadPlayerMultipliers(e.getPlayer());
                }).bindWith(this.multipliers.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    this.rankMultipliers.remove(e.getPlayer().getUniqueId());
                    savePlayerMultipliers(e.getPlayer(), true, true);
                }).bindWith(this.multipliers.getPlugin());
        loadRankMultipliers();
        loadOnlineMultipliers();
        this.rankUpdateTask = Schedulers.async().runRepeating(() -> {
            Players.all().forEach((player -> this.rankMultipliers.put(player.getUniqueId(), calculateRankMultiplier(player))));
        }, 5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES);
    }

    private void loadRankMultipliers() {
        this.permissionToMultiplier = new LinkedHashMap<>();
        ConfigurationSection section = this.multipliers.getConfig().getConfigurationSection("ranks");
        for (String string : section.getKeys(false)) {
            String permission = "group." + string;
            this.permissionToMultiplier.put(permission, new RankMultiplier(
                    section.getDouble(string + ".Money"),
                    section.getDouble(string + ".Tokens"),
                    section.getDouble(string + ".Gems"),
                    section.getDouble(string + ".Exp")
            ));
        }
    }

    private void loadOnlineMultipliers() {
        Players.all().forEach((player -> {
            this.rankMultipliers.put(player.getUniqueId(), calculateRankMultiplier(player));
            loadPlayerMultipliers(player);
        }));
    }

    private void loadPlayerMultipliers(Player player) {
        Schedulers.async().run(() -> {
            PlayerMultiplier playerMultiplier = this.multipliers.getPlugin().getPluginDatabase().getPlayerMultiplier(player);
            if (playerMultiplier != null) this.playerMultipliers.put(player.getUniqueId(), playerMultiplier);
            this.multipliers.getPlugin().getLogger().info(String.format("Loaded multiplier for %s.", player.getName()));
        });
    }

    private void savePlayerMultipliers(Player player, boolean runAsync, boolean removeFromCache) {
        if (!(this.playerMultipliers.containsKey(player.getUniqueId()))) return;
        if (runAsync)
            Schedulers.async().run(() -> this.multipliers.getPlugin().getPluginDatabase().savePlayerMultiplier(player, this.playerMultipliers.get(player.getUniqueId())));
        else
            this.multipliers.getPlugin().getPluginDatabase().savePlayerMultiplier(player, this.playerMultipliers.get(player.getUniqueId()));
        if (removeFromCache) this.playerMultipliers.remove(player.getUniqueId());
    }

    public void saveAllMultipliers() {
        Players.all().forEach((player -> savePlayerMultipliers(player, false, false)));
    }

    private void removeExpiredMultipliers() {
        Schedulers.async().run(() -> {
            this.multipliers.getPlugin().getPluginDatabase().removeExpiredMultipliers();
            this.multipliers.getPlugin().getLogger().info("Removed expired multipliers.");
        });
    }

    public void resetPlayerMultiplier(Player player, MultiplierType multiplierType) {
        this.playerMultipliers.get(player.getUniqueId()).getMultiplierSet().getMulti(multiplierType).reset();
    }

    public void addPlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType multiplierType) {
        this.playerMultipliers.get(player.getUniqueId()).getMultiplierSet().getMulti(multiplierType).addMultiplier(multi);
        this.playerMultipliers.get(player.getUniqueId()).getMultiplierSet().getMulti(multiplierType).addDuration(timeUnit, time);
    }

    public PlayerMultiplier getPlayerMultiplier(Player player) {
        return this.playerMultipliers.get(player.getUniqueId());
    }

    public RankMultiplier getRankMultiplier(Player player) {
        return this.rankMultipliers.get(player.getUniqueId());
    }

    private RankMultiplier calculateRankMultiplier(Player player) {
        RankMultiplier rankMultiplier = null;
        for (String permission : this.permissionToMultiplier.keySet())
            if (player.hasPermission(permission)) rankMultiplier = this.permissionToMultiplier.get(permission);
        return rankMultiplier;
    }
}
