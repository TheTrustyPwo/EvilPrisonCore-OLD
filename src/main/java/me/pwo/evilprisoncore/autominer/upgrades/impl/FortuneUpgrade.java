package me.pwo.evilprisoncore.autominer.upgrades.impl;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.autominer.upgrades.AutoMinerUpgrade;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class FortuneUpgrade extends AutoMinerUpgrade {
    private double chance;
    private long baseTokens;
    private long perLevelTokens;

    public FortuneUpgrade(AutoMiner autoMiner) {
        super(autoMiner, 1);
    }

    @Override
    public void onAutoMine(Player player, int level) {
        if (this.chance * level >= ThreadLocalRandom.current().nextDouble(100.0D)) {
            EvilPrisonCore.getInstance().getTokens().getApi().addTokens(player, this.baseTokens + level * perLevelTokens, true);
        }

    }

    @Override
    public void reload() {
        this.chance = autoMiner.getConfig().getDouble("autominer_upgrades." + this.id + ".Chance");
        this.baseTokens = autoMiner.getConfig().getLong("autominer_upgrades." + this.id + ".Base-Tokens");
        this.perLevelTokens = autoMiner.getConfig().getLong("autominer_upgrades." + this.id + ".Per-Level-Tokens");
    }
}
