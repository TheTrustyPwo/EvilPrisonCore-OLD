package me.pwo.evilprisoncore.privatemines.manager;

import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.utils.Direction;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PrivateMinesWorldManager {
    private static final int minesDistance = 400;
    private final PrivateMines privateMines;
    private int distance;
    private Direction direction;
    private World minesWorld;
    private Location defaultLocation;

    public PrivateMinesWorldManager(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.distance = this.privateMines.getMinesDistance();
        this.direction = this.privateMines.getMineDirection();
        createMinesWorld();
    }

    private void createMinesWorld() {
        this.minesWorld = Bukkit.createWorld(new WorldCreator("private-mines")
                .type(WorldType.FLAT)
                .generator(new VoidWorldGenerator()));
        this.defaultLocation = new Location(this.minesWorld, 0.0D, 100.0D, 0.0D);
    }

    public synchronized Location getNextFreeLocation() {
        if (this.distance == 0) {
            this.distance++;
            this.privateMines.saveMinesDistance(this.distance);
            return this.defaultLocation;
        }
        if (this.direction == null)
            this.direction = Direction.NORTH;
        Location location = direction.addTo(this.defaultLocation, this.distance * minesDistance);
        this.direction = this.direction.next();
        if (this.direction == Direction.NORTH) {
            this.distance ++;
            this.privateMines.saveMinesDistance(this.distance);
        }
        this.privateMines.saveMineDirection(this.direction);
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
