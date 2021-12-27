package me.pwo.evilprisoncore.privatemines.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class LegacyWEHook implements WorldEditHook {
    public static Region transform(WorldEditRegion region) {
        return new CuboidRegion(
                FaweAPI.getWorld(region.getWorld().getName()),
                transform(region.getMinimumPoint()),
                transform(region.getMaximumPoint()));
    }

    public static Vector transform(WorldEditVector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static WorldEditVector transform(Vector vector) {
        return new WorldEditVector(vector.getX(), vector.getY(), vector.getZ());
    }

    public void fill(WorldEditRegion region, Material block) throws MaxChangedBlocksException {
        EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(region.getWorld().getName()))).fastmode(Boolean.TRUE).build();
        session.setBlocks(transform(region), new BaseBlock(block.getId()));
        session.flushQueue();
    }

    public MineFactoryCompat<?> createMineFactoryCompat() {
        return new LegacyWEMineFactoryCompat(PrivateMines.getInstance().getPrivateMinesWorldManager());
    }

    public MineSchematic<?> loadMineSchematic(String name, List<String> description, File file, ItemStack item) {
        return new LegacyWEMineSchematic(name, description, file, item);
    }
}
