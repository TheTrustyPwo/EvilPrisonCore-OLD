package me.pwo.evilprisoncore.menu.settings;

import java.util.UUID;

public class Settings {
    private final UUID player;
    private int numberDisplayType;

    public Settings(UUID player, int numberDisplayType) {
        this.player = player;
        this.numberDisplayType = numberDisplayType;
    }

    public UUID getPlayer() {
        return player;
    }

    public int getNumberDisplayType() {
        return numberDisplayType;
    }

    public void setNumberDisplayType(int numberDisplayType) {
        this.numberDisplayType = numberDisplayType;
    }
}
