package me.pwo.evilprisoncore.privatemines.worldedit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Map;

public class WorldEditUtil {
    public static Vector toBukkitVector(WorldEditVector weVector) {
        return new Vector(weVector.getX(), weVector.getY(), weVector.getZ());
    }

    public static WorldEditVector toWEVector(Vector bukkitVector) {
        return new WorldEditVector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static WorldEditVector toWEVector(Location bukkitVector) {
        return new WorldEditVector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static Location toLocation(WorldEditVector weVector, World world) {
        return new Location(world, weVector.getX(), weVector.getY(), weVector.getZ());
    }

    public static WorldEditVector deserializeWorldEditVector(Map<String, Object> map) {
        return toWEVector(Vector.deserialize(map));
    }

    public static Float getYaw(BlockFace face) {
        switch (face) {
            case WEST:
                return 90.0F;
            case NORTH:
                return 180.0F;
            case EAST:
                return -90.0F;
            case SOUTH:
                return -180.0F;
        }
        return 0.0F;
    }
}
