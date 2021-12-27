package me.pwo.evilprisoncore.privatemines.manager;

import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.utils.Direction;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class PrivateMinesWorldManager {
    private final PrivateMines privateMines;
    private static final int minesDistance = 200;
    private int distance;
    private Direction direction;
    private World minesWorld;
    private Location defaultLocation;
    IWrappedRegion globalRegion;

    public PrivateMinesWorldManager(PrivateMines privateMines) {
        this.privateMines = privateMines;
        createMinesWorld();
    }

    private void createMinesWorld() {
        this.minesWorld = Bukkit.createWorld(new WorldCreator("private-mines")
                .type(WorldType.FLAT)
                .generator(new VoidWorldGenerator()));
        this.direction = Direction.NORTH;
        this.defaultLocation = new Location(this.minesWorld, 0.0D, 50.0D, 0.0D);
        if (WorldGuardWrapper.getInstance().getRegion(this.minesWorld, "__global__").isPresent()) {
            this.globalRegion = WorldGuardWrapper.getInstance().getRegion(this.minesWorld, "__global__").get();
            this.globalRegion.setFlag(WorldGuardWrapper.getInstance().getFlag("build", WrappedState.class).get(), WrappedState.DENY);
            this.globalRegion.setFlag(WorldGuardWrapper.getInstance().getFlag("interact", WrappedState.class).get(), WrappedState.DENY);
            this.globalRegion.setFlag(WorldGuardWrapper.getInstance().getFlag("use", WrappedState.class).get(), WrappedState.DENY);
        } else {
            this.privateMines.getPlugin().getLogger().warning("The global region is somehow null.");
        }
    }

    public synchronized Location getNextFreeLocation() {
        if (this.distance == 0) {
            this.distance++;
            return this.defaultLocation;
        }
        Location location = direction.addTo(this.defaultLocation, this.distance * minesDistance);
        this.direction = this.direction.next();
        if (this.direction == Direction.NORTH) this.distance ++;
        return location;
    }

    public World getMinesWorld() {
        return minesWorld;
    }

    private static class VoidWorldGenerator extends ChunkGenerator {
        @NotNull
        public ChunkData generateChunkData(@NotNull World paramWorld, @NotNull Random paramRandom, int paramInt1, int paramInt2, @NotNull ChunkGenerator.BiomeGrid paramBiomeGrid) {
            return createChunkData(paramWorld);
        }
    }
}
