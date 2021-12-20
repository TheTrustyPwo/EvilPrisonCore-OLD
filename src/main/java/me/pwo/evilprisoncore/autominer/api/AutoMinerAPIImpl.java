package me.pwo.evilprisoncore.autominer.api;

import me.pwo.evilprisoncore.autominer.AutoMiner;
import org.bukkit.entity.Player;

public class AutoMinerAPIImpl implements AutoMinerAPI {
    private final AutoMiner autoMiner;

    public AutoMinerAPIImpl(AutoMiner autoMiner) {
        this.autoMiner = autoMiner;
    }

    public boolean isInAutoMinerRegion(Player paramPlayer) {
        return this.autoMiner.isInAutoMinerRegion(paramPlayer);
    }

    public int getAutoMinerTime(Player paramPlayer) {
        return this.autoMiner.getAutoMinerTime(paramPlayer);
    }
}
