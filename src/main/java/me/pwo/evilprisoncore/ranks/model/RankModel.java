package me.pwo.evilprisoncore.ranks.model;

import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.beans.ConstructorProperties;
import java.util.List;

public class RankModel {
    private final long id;
    private final double cost;
    private final String prefix;
    private final List<String> commandsToExecute;
    private final int mineSize;
    private final List<Material> unlockedBlocks;

    @ConstructorProperties({"id", "cost", "prefix", "commandsToExecute", "mineSize", "unlockedBlocks"})
    public RankModel(long id, double cost, String prefix, List<String> commandsToExecute, int mineSize, List<Material> unlockedBlocks) {
        this.id = id;
        this.cost = cost;
        this.prefix = prefix;
        this.commandsToExecute = commandsToExecute;
        this.mineSize = mineSize;
        this.unlockedBlocks = unlockedBlocks;
    }

    public long getId() {
        return this.id;
    }

    public double getCost() {
        return this.cost;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<String> getCommandsToExecute() {
        return this.commandsToExecute;
    }

    public void runCommands(Player paramPlayer) {
        if (this.commandsToExecute != null)
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.async().run(() -> executeCommands(paramPlayer));
            } else {
                executeCommands(paramPlayer);
            }
    }

    private void executeCommands(Player paramPlayer) {
        for (String str : this.commandsToExecute)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%player%", paramPlayer.getName()).replace("%Prestige%", this.prefix).replace("%Rank%", this.prefix));
    }

    public int getMineSize() {
        return mineSize;
    }

    public List<Material> getUnlockedBlocks() {
        return unlockedBlocks;
    }
}
