package me.pwo.evilprisoncore.multipliers.manager;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.GlobalMultiplier;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MultipliersManager {
    private final Multipliers multipliers;
    private GlobalMultiplier globalSellMultiplier;
    private GlobalMultiplier globalTokenMultiplier;
    private final HashMap<UUID, RankMultiplier> rankMultipliers = new HashMap<>();
    private final HashMap<UUID, List<Multiplier>> playerMultipliers = new HashMap<>();
    private LinkedHashMap<String, RankMultiplier> permissionToMultiplier = new LinkedHashMap<>();
    private int idCounter;
    private Task rankUpdateTask;

    public MultipliersManager(Multipliers multipliers) {
        this.multipliers = multipliers;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> loadPlayerMultipliers(e.getPlayer())).bindWith(this.multipliers.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerMultipliers(e.getPlayer(), true, true)).bindWith(this.multipliers.getPlugin());
        loadRankMultipliers();
        removeExpiredMultipliers();
        loadOnlineMultipliers();
        updateRankMultiplier();
    }

    private void loadIdCounter() {
        this.idCounter = this.multipliers.getPlugin().getPluginDatabase().getNextAutoIncrementValue("EvilPrison_Multipliers");
    }

    private void updateRankMultiplier() {
        this.rankUpdateTask = Schedulers.async().runRepeating(
                () -> Players.all().forEach(
                        (player -> this.rankMultipliers.put(player.getUniqueId(),
                                calculateRankMultiplier(player)))), 5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES);
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
            List<Multiplier> multipliers = this.multipliers.getPlugin().getPluginDatabase().getPlayerMultipliers(player);
            this.playerMultipliers.put(player.getUniqueId(), multipliers);
            this.rankMultipliers.put(player.getUniqueId(), calculateRankMultiplier(player));
            this.multipliers.getPlugin().getLogger().info(String.format("Loaded multiplier for %s.", player.getName()));
        });
    }

    private void savePlayerMultipliers(Player player, boolean runAsync, boolean removeFromCache) {
        if (!(this.playerMultipliers.containsKey(player.getUniqueId()))) return;
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.multipliers.getPlugin().getPluginDatabase().savePlayerMultipliers(player, this.playerMultipliers.get(player.getUniqueId()));
                if (removeFromCache) {
                    this.playerMultipliers.remove(player.getUniqueId());
                    this.rankMultipliers.remove(player.getUniqueId());
                }
                this.multipliers.getPlugin().getLogger().info(String.format("Saved multiplier for %s.", player.getName()));
            });
        } else {
            this.multipliers.getPlugin().getPluginDatabase().savePlayerMultipliers(player, this.playerMultipliers.get(player.getUniqueId()));
            if (removeFromCache) {
                this.playerMultipliers.remove(player.getUniqueId());
                this.rankMultipliers.remove(player.getUniqueId());
            }
            this.multipliers.getPlugin().getLogger().info(String.format("Saved multiplier for %s.", player.getName()));
        }
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

    public void addPlayerMultiplier(Player player, int id, double multi, TimeUnit timeUnit, int time) {
         Multiplier multiplier = this.playerMultipliers.get(player.getUniqueId()).stream().filter(m -> m.getId() == id)
                .collect(Collectors.toList()).get(0);
         multiplier.addMultiplier(multi);
         multiplier.addDuration(timeUnit, time);
    }

    public void givePlayerMultiplier(Player player, double amount, TimeUnit timeUnit, int time, MultiplierType multiplierType, MultiplierSource multiplierSource) {
        this.playerMultipliers.get(player.getUniqueId()).add(
                new Multiplier(++this.idCounter,
                        amount, timeUnit, time, multiplierType, multiplierSource)
        );
    }

    public void deletePlayerMultiplier(Player player, int id) {
        this.playerMultipliers.get(player.getUniqueId()).remove(this.playerMultipliers.get(player.getUniqueId())
                .stream().filter(multiplier -> multiplier.getId() == id)
                        .collect(Collectors.toList()).get(0));
    }

    public List<Multiplier> getPlayerMultipliers(Player player) {
        return this.playerMultipliers.get(player.getUniqueId());
    }

    public List<Multiplier> getPlayerMultipliers(Player player, MultiplierType multiplierType) {
        return this.playerMultipliers.get(player.getUniqueId())
                .stream().filter(multiplier -> multiplier.getMultiplierType() == multiplierType).collect(Collectors.toList());
    }

    public double getTotalPlayerMultiplier(Player player, MultiplierType multiplierType) {
        List<Multiplier> multipliers = getPlayerMultipliers(player).stream()
                .filter(multiplier -> !(multiplier.isExpired()))
                .filter(multiplier -> multiplier.getMultiplierType() == multiplierType)
                .sorted(Comparator.comparingInt(multiplier -> MultiplierSource.getPriority(multiplier.getMultiplierSource())))
                .collect(Collectors.toList());
        double amount = 0.0D;
        for (Multiplier multiplier : multipliers) amount += multiplier.getMultiplier();
        return amount + getRankMultiplier(player).getMulti(multiplierType);
    }

    public long getPlayerMultiplierAmount(Player player, MultiplierType multiplierType) {
        return getPlayerMultipliers(player).stream()
                .filter(multiplier -> !(multiplier.isExpired()))
                .filter(multiplier -> multiplier.getMultiplierType() == multiplierType).count();
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
