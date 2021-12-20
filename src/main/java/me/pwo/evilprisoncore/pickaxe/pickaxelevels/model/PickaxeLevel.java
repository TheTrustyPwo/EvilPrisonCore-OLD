package me.pwo.evilprisoncore.pickaxe.pickaxelevels.model;

import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.beans.ConstructorProperties;
import java.util.List;

public class PickaxeLevel {
    private final int level;

    private final long blocksRequired;

    private final String displayName;

    private final List<String> rewards;

    @ConstructorProperties({"level", "blocksRequired", "displayName", "rewards"})
    public PickaxeLevel(int paramInt, long paramLong, String paramString, List<String> paramList) {
        this.level = paramInt;
        this.blocksRequired = paramLong;
        this.displayName = paramString;
        this.rewards = paramList;
    }

    public int getLevel() {
        return this.level;
    }

    public long getBlocksRequired() {
        return this.blocksRequired;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void giveRewards(Player paramPlayer) {
        if (!Bukkit.isPrimaryThread()) {
            Schedulers.sync().run(() -> this.rewards.forEach(paramString -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), paramString.replace("%player%", paramPlayer.getName()))));
        } else {
            this.rewards.forEach(paramString -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), paramString.replace("%player%", paramPlayer.getName())));
        }
    }

    public String getDisplayName(Player paramPlayer) {
        return this.displayName.replace("%player%", paramPlayer.getName());
    }
}
