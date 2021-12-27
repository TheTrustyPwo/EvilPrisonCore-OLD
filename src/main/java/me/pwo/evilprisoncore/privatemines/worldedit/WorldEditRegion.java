package me.pwo.evilprisoncore.privatemines.worldedit;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldEditRegion {
    private final WorldEditVector min;

    private final WorldEditVector max;

    private final WorldEditVector center;

    private final World world;

    public WorldEditRegion(WorldEditVector min, WorldEditVector max, World world) {
        this.min = min;
        this.max = max;
        this.world = world;
        this.center = new WorldEditVector((min.getX() + max.getX()) / 2.0D, (min.getY() + max.getY()) / 2.0D, (min.getZ() + max.getZ()) / 2.0D);
    }

    public WorldEditVector getMinimumPoint() {
        return this.min;
    }

    public Location getMinimumLocation() {
        return new Location(getWorld(), this.min.getX(), this.min.getY(), this.min.getZ());
    }

    public WorldEditVector getMaximumPoint() {
        return this.max;
    }

    public Location getMaximumLocation() {
        return new Location(getWorld(), this.max.getX(), this.max.getY(), this.max.getZ());
    }

    public String toString() {
        return "WorldEditRegion{min=" + this.min + ", max=" + this.max + ", center=" + this.center + ", world=" + this.world + '}';
    }

    public boolean contains(WorldEditVector vector) {
        return (vector.getX() >= this.min.getX() && vector.getX() <= this.max.getX() && vector
                .getY() >= this.min.getY() && vector.getY() <= this.max.getY() && vector
                .getZ() >= this.min.getZ() && vector.getZ() <= this.max.getZ());
    }

    public World getWorld() {
        return this.world;
    }

    public WorldEditVector getCenter() {
        return this.center;
    }
}
