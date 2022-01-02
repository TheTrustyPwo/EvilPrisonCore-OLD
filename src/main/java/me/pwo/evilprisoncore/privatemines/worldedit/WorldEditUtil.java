package me.pwo.evilprisoncore.privatemines.worldedit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Map;

public class WorldEditUtil {
    public static Vector toBukkitVector(com.sk89q.worldedit.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static com.sk89q.worldedit.Vector toWEVector(Vector bukkitVector) {
        return new com.sk89q.worldedit.Vector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static com.sk89q.worldedit.Vector toWEVector(Location location) {
        return new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
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

    public static Location toLocation(com.sk89q.worldedit.Vector vector, World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }
}
