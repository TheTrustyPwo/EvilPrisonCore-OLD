package me.pwo.evilprisoncore.privatemines.api;

import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import org.bukkit.Location;

import java.util.stream.Collectors;

public class PrivateMinesAPIImpl implements PrivateMinesAPI {
    private final PrivateMines privateMines;

    public PrivateMinesAPIImpl(PrivateMines privateMines) {
        this.privateMines = privateMines;
    }

    @Override
    public Mine getMineByLocation(Location location) {
        return this.privateMines.getPrivateMinesManager().getAllMines().values().stream().filter(mine -> mine.getMainRegion().contains(WorldEditUtil.toWEVector(location))).collect(Collectors.toList()).get(0);
    }
}
