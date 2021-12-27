package me.pwo.evilprisoncore.privatemines.worldedit;

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public interface WorldEditHook {
    void fill(WorldEditRegion paramWorldEditRegion, Material paramMaterial) throws MaxChangedBlocksException;

    MineFactoryCompat<?> createMineFactoryCompat();

    MineSchematic<?> loadMineSchematic(String paramString, List<String> paramList, File paramFile, ItemStack paramItemStack);
}
