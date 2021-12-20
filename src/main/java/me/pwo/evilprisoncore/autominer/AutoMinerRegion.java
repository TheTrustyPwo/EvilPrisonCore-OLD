package me.pwo.evilprisoncore.autominer;

import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoMinerRegion {
    private final AutoMiner parent;
    private final World world;
    private final IWrappedRegion region;
    private final List<String> commands;
    private final Task autoMinerTask;

    public AutoMiner getParent() {
        return this.parent;
    }

    public World getWorld() {
        return this.world;
    }

    public IWrappedRegion getRegion() {
        return this.region;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public Task getAutoMinerTask() {
        return this.autoMinerTask;
    }

    public AutoMinerRegion(AutoMiner paramUltraPrisonAutoMiner, World paramWorld, IWrappedRegion paramIWrappedRegion, List<String> paramList, int paramInt) {
        this.parent = paramUltraPrisonAutoMiner;
        this.world = paramWorld;
        this.region = paramIWrappedRegion;
        this.commands = paramList;
        AtomicInteger atomicInteger = new AtomicInteger();
        this.autoMinerTask = Schedulers.async().runRepeating(() -> {
            int i = atomicInteger.getAndIncrement();
            for (Player player : Players.all()) {
                if (!player.getWorld().equals(this.world))
                    continue;
                if (paramIWrappedRegion.contains(player.getLocation())) {
                    if (!paramUltraPrisonAutoMiner.hasAutoMinerTime(player)) {
                        PlayerUtils.sendActionBar(player, paramUltraPrisonAutoMiner.getMessage("auto_miner_disabled"));
                        continue;
                    }
                    PlayerUtils.sendActionBar(player, paramUltraPrisonAutoMiner.getMessage("auto_miner_enabled"));
                    if (i >= paramInt)
                        executeCommands(player);
                    this.parent.decrementTime(player);
                }
            }
            if (i >= paramInt)
                atomicInteger.set(0);
        }, 1L, TimeUnit.SECONDS, 1L, TimeUnit.SECONDS);
    }

    private void executeCommands(Player paramPlayer) {
        if (this.commands.isEmpty())
            return;
        this.commands.forEach(paramString -> Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), paramString.replace("%player%", paramPlayer.getName()))));
    }
}
