package me.pwo.evilprisoncore.autominer.upgrades.impl;

import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.autominer.upgrades.AutoMinerUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScavengerUpgrade extends AutoMinerUpgrade {
    private double chance;
    private List<String> commandsToExecute;

    public ScavengerUpgrade(AutoMiner autoMiner) {
        super(autoMiner, 2);
    }

    @Override
    public void onAutoMine(Player player, int level) {
        if (this.chance * level >= ThreadLocalRandom.current().nextDouble(100.0D)) {
            String str = this.commandsToExecute.get(ThreadLocalRandom.current().nextInt(this.commandsToExecute.size()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%player%", player.getName()));
        }

    }

    @Override
    public void reload() {
        this.chance = autoMiner.getConfig().getDouble("autominer_upgrades." + this.id + ".Chance");
        this.commandsToExecute = autoMiner.getConfig().getStringList("autominer_upgrades." + this.id + ".Commands");
    }
}
