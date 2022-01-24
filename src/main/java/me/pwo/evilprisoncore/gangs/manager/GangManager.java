package me.pwo.evilprisoncore.gangs.manager;

import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GangManager {
    private static final List<String> GANGS_TOP_FORMAT = Arrays.asList(
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
            "&e&lGANGS TOP",
            "{FOR_EACH_GANG} &f&l#%position%. &e%gang% &8&7%value% Trophies",
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"
    );
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
        loadGangs();
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

    public void invitePlayer(Player player, Gang gang) {
        this.pendingInvites.put(player.getUniqueId(), gang);
        Schedulers.async().runLater(() -> this.pendingInvites.remove(player), 5L, TimeUnit.MINUTES);
    }

    public void acceptPlayer(Player player) {
        this.pendingInvites.get(player.getUniqueId()).addPlayer(player);
        this.pendingInvites.remove(player.getUniqueId());
    }

    public boolean toggleGangChat(Player player) {
        if (this.gangChatEnabledPlayers.contains(player.getUniqueId())) {
            this.gangChatEnabledPlayers.remove(player.getUniqueId());
            return false;
        }
        this.gangChatEnabledPlayers.add(player.getUniqueId());
        return true;
    }

    public boolean hasGangChatEnabled(Player player) {
        return this.gangChatEnabledPlayers.contains(player.getUniqueId());
    }

    public void gangChat(Player player, String message) {
        Optional<Gang> gang = getPlayerGang(player);
        if (!gang.isPresent()) {
            this.gangChatEnabledPlayers.remove(player.getUniqueId());
            return;
        }
        gang.get().broadcastToMembers("%prefix% &f%player% &8» %message%"
                .replaceAll("%prefix%", "&e&l[GC]")
                .replaceAll("%player%", player.getName())
                .replaceAll("%message%", message));
    }

    public Map<UUID, Gang> getPendingInvites() {
        return this.pendingInvites;
    }

    private void updateTop10() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            this.topGangs = this.gangs.values().stream().sorted(Comparator.comparingLong(Gang::getGangTrophies).reversed()).collect(Collectors.toList());
            this.updating = false;
        }, 5L, TimeUnit.MINUTES, 5L, TimeUnit.MINUTES);
    }

    public void sendGangTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, "&c&lLeaderboard is currently updating...");
            return;
        }
        for (String string : GANGS_TOP_FORMAT) {
            if (string.startsWith("{FOR_EACH_GANG}")) {
                string = string.replace("{FOR_EACH_GANG}", "");
                for (byte position = 0; position <= 10; position++) {
                    try {
                        Gang gang = this.topGangs.get(position);
                        PlayerUtils.sendMessage(sender, string
                                .replaceAll("%position%", String.valueOf(position + 1))
                                .replaceAll("%gang%", gang.getGangName())
                                .replaceAll("%trophies%", Utils.formatNumber(gang.getGangTrophies())));
                    } catch (Exception e) { break; }
                } continue;
            } PlayerUtils.sendMessage(sender, string);
        }
    }
}
