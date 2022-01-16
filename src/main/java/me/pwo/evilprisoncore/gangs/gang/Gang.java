package me.pwo.evilprisoncore.gangs.gang;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Gang {
    private UUID gangId;
    private String gangName;
    private UUID gangOwner;
    private List<UUID> gangMembers;
    private long gangTrophies;

    public Gang(String gangName, UUID gangOwner) {
        this.gangId = UUID.randomUUID();
        this.gangName = gangName;
        this.gangOwner = gangOwner;
        this.gangMembers = new ArrayList<>();
        this.gangTrophies = 0L;
    }

    public Gang(UUID gangId, String gangName, UUID gangOwner, List<UUID> gangMembers, long gangTrophies) {
        this.gangId = gangId;
        this.gangName = gangName;
        this.gangOwner = gangOwner;
        this.gangMembers = gangMembers;
        this.gangTrophies = gangTrophies;
    }

    public UUID getGangId() {
        return gangId;
    }

    public void setGangId(UUID gangId) {
        this.gangId = gangId;
    }

    public String getGangName() {
        return gangName;
    }

    public void setGangName(String gangName) {
        this.gangName = gangName;
    }

    public UUID getGangOwner() {
        return gangOwner;
    }

    public void setGangOwner(UUID gangOwner) {
        this.gangOwner = gangOwner;
    }

    public List<UUID> getGangMembers() {
        return gangMembers;
    }

    public void setGangMembers(List<UUID> gangMembers) {
        this.gangMembers = gangMembers;
    }

    public long getGangTrophies() {
        return gangTrophies;
    }

    public void setGangTrophies(long gangTrophies) {
        this.gangTrophies = gangTrophies;
    }

    public boolean containsPlayer(@NotNull OfflinePlayer player) {
        return (this.gangOwner.equals(player.getUniqueId()) || this.gangMembers.contains(player.getUniqueId()));
    }

    public boolean isOwner(@NotNull OfflinePlayer player) {
        return player.getUniqueId().equals(this.gangOwner);
    }

    public void addPlayer(@NotNull OfflinePlayer player) {
        if (this.gangMembers.contains(player.getUniqueId())) return;
        this.gangMembers.add(player.getUniqueId());
    }

    public void removePlayer(@NotNull OfflinePlayer player) {
        if (!this.gangMembers.contains(player.getUniqueId())) return;
        this.gangMembers.remove(player.getUniqueId());
    }

    public List<Player> getOnlinePlayers() {
        return Players.all().stream().filter(this::containsPlayer).collect(Collectors.toList());
    }

    public List<OfflinePlayer> getMembersOffline() {
        List<OfflinePlayer> membersOffline = new ArrayList<>();
        this.gangMembers.forEach(member -> membersOffline.add(Players.getOfflineNullable(member)));
        return membersOffline;
    }

    public OfflinePlayer getOwnerOffline() {
        return Players.getOfflineNullable(this.gangOwner);
    }

    public void disband() {
        this.gangMembers.clear();
        this.gangOwner = null;
    }
}
