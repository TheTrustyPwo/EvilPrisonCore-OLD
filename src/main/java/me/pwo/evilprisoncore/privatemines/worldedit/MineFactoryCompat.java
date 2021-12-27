package me.pwo.evilprisoncore.privatemines.worldedit;

import org.bukkit.Location;

public interface MineFactoryCompat<S> {
    WorldEditRegion pasteSchematic(S paramS, Location paramLocation);

    Iterable<WorldEditVector> loop(WorldEditRegion paramWorldEditRegion);
}
