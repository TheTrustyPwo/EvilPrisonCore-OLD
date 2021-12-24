package me.pwo.evilprisoncore.blocks.blockrewards.model;

import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockReward {
    private final long blocksRequired;
    private final List<String> rewards;
    private final List<String> commandsToRun;

    public BlockReward(long blocksRequired, List<String> rewards, List<String> commandsToRun) {
        this.blocksRequired = blocksRequired;
        this.rewards = rewards;
        this.commandsToRun = commandsToRun;
    }

    public long getBlocksRequired() {
        return blocksRequired;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public List<String> getCommandsToRun() {
        return commandsToRun;
    }

    public void runCommands(Player player) {
        if (Bukkit.isPrimaryThread()) {
            for (String str : this.commandsToRun)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%player%", player.getName()));
        } else {
            Schedulers.sync().run(() -> {
                for (String str : this.commandsToRun)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%player%", player.getName()));
            });
        }
    }
}
