package me.pwo.evilprisoncore.multipliers.model;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerMultiplier {
    private final UUID playerUUID;
    protected MultiplierSet multiplierSet;
    protected MultiplierSet rankMultiplier;

    public PlayerMultiplier(UUID uuid, MultiplierSet multiplierSet) {
        this.playerUUID = uuid;
        this.multiplierSet = multiplierSet;
    }

    public MultiplierSet getMultiplierSet() {
        return multiplierSet;
    }

    public void setMultiplierSet(MultiplierSet multiplierSet) {
        this.multiplierSet = multiplierSet;
    }

    public MultiplierSet getRankMultiplier() {
        return rankMultiplier;
    }

    public void setRankMultiplier(MultiplierSet rankMultiplier) {
        this.rankMultiplier = rankMultiplier;
    }
}
