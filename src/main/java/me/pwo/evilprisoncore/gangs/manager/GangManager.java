package me.pwo.evilprisoncore.gangs.manager;

import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GangManager {
    private final EvilPrisonGangs evilPrisonGangs;
    private Map<UUID, Gang> gangs;
    private Map<UUID, Gang> pendingInvites;
    private List<UUID> gangChatEnabledPlayers;
    private boolean updating;
    private List<Gang> topGangs;
    private Task task;

    public EvilPrisonGangs getEvilPrisonGangs() {
        return evilPrisonGangs;
    }

    public GangManager(EvilPrisonGangs evilPrisonGangs) {
        this.evilPrisonGangs = evilPrisonGangs;
    }

    private void loadGangs() {
        this.gangs = new HashMap<>();
        Schedulers.async().run(() -> {
            for (Gang gang : this.evilPrisonGangs.getPlugin().getPluginDatabase().getAllGangs())
                this.gangs.put(gang.getGangId(), gang);
        });
    }

    public void saveDataOnDisable() {
        for (Gang gang : this.gangs.values())
            this.evilPrisonGangs.getPlugin().getPluginDatabase().updateGang(gang);
    }

    public Optional<Gang> getPlayerGang(OfflinePlayer player) {
        return this.gangs.values().stream().filter(gang -> gang.containsPlayer(player)).findFirst();
    }

    public Optional<Gang> getGangWithName(String gangName) {
        return this.gangs.values().stream().filter(gang -> Text.decolorize(gang.getGangName()).equals(gangName)).findFirst();
    }

    public void createGang(String gangName, OfflinePlayer player) {
        this.gangs.put(player.getUniqueId(), new Gang(gangName, player.getUniqueId()));
    }

    public void removeGang(OfflinePlayer player) {
        Optional<Gang> gang = getPlayerGang(player);
        if (!gang.isPresent()) return;
        this.gangs.remove(gang.get().getGangId());
    }

    private void updateTop10() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            this.topGangs = this.gangs.values().stream().sorted(Comparator.comparingLong(Gang::getGangTrophies).reversed()).collect(Collectors.toList());
            this.updating = false;
        }, 5L, TimeUnit.MINUTES, 5L, TimeUnit.MINUTES);
    }
}
