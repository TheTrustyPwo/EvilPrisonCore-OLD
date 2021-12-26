package me.pwo.evilprisoncore.multipliers.manager;

import de.schlichtherle.crypto.io.raes.RaesKeyException;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.terminable.TerminableConsumer;
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

public class MultipliersManager {
    private final Multipliers multipliers;
    private GlobalMultiplier globalSellMultiplier;
    private GlobalMultiplier globalTokenMultiplier;
    private HashMap<UUID, RankMultiplier> rankMultipliers;
    private HashMap<UUID, PlayerMultiplier> playerMultipliers;
    private HashMap<String, String> messages;
    private LinkedHashMap<String, RankMultiplier> permissionToMultiplier;
    private Task rankUpdateTask;
    private int rankMultiplierUpdateTime;

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
        }));
    }

    private void loadPlayerMultipliers(Player player) {
        Schedulers.async().run(() -> {
            PlayerMultiplier playerMultiplier = this.multipliers.getPlugin().getPluginDatabase().getPlayerMultiplier(player);
            if (playerMultiplier != null) this.playerMultipliers.put(player.getUniqueId(), playerMultiplier);
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

    private void saveAllMultipliers() {
        Players.all().forEach((player -> savePlayerMultipliers(player, false, false)));
    }

    private void removeExpiredMultipliers() {
        Schedulers.async().run(() -> {
            this.multipliers.getPlugin().getPluginDatabase().removeExpiredMultipliers();
            this.multipliers.getPlugin().getLogger().info("Removed expired multipliers.");
        });
    }

    private RankMultiplier calculateRankMultiplier(Player player) {
        RankMultiplier rankMultiplier = null;
        for (String permission : this.permissionToMultiplier.keySet())
            if (player.hasPermission(permission)) rankMultiplier = this.permissionToMultiplier.get(permission);
        return rankMultiplier;
    }
}
