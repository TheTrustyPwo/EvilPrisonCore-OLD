package me.pwo.evilprisoncore.privatemines.manager;

import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;

import java.util.HashMap;
import java.util.UUID;

public class PrivateMinesManager {
    private PrivateMines privateMines;
    private HashMap<UUID, Mine> mines;

    public PrivateMinesManager(PrivateMines privateMines) {
        this.privateMines = privateMines;
    }

    private void loadMines() {
        Schedulers.async().run(() -> {
            for (Mine mine : this.privateMines.getPlugin().getPluginDatabase().getAllMineData())
                mines.put(mine.getOwner(), mine);
        });
    }
}
