package me.pwo.evilprisoncore.autominer.api;

import org.bukkit.entity.Player;

public interface AutoMinerAPI {
    boolean isInAutoMinerRegion(Player paramPlayer);

    int getAutoMinerTime(Player paramPlayer);
}
