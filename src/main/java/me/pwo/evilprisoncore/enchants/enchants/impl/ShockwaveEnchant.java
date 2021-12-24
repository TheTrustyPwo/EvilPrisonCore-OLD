package me.pwo.evilprisoncore.enchants.enchants.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import me.pwo.evilprisoncore.utils.RegionUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ShockwaveEnchant extends EvilPrisonEnchantment {
    private double chance;

    public ShockwaveEnchant(Enchants enchants) {
        super(enchants, 10);
    }

    @Override
    public void onEquip(Player player, ItemStack itemStack, int level) {}

    @Override
    public void onUnequip(Player player, ItemStack itemStack, int level) {}

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (this.chance * level >= random) {
            Block block = e.getBlock();
            IWrappedRegion iWrappedRegion = RegionUtils.getMineRegionWithHighestPriority(block.getLocation());
            ICuboidSelection iCuboidSelection = (ICuboidSelection) iWrappedRegion.getSelection();
            double totalProfit = 0.0D;
            List<Block> blocks = new ArrayList<>();
            int fortuneLevel = this.enchants.getEnchantsManager().getEnchantLevel(e.getPlayer().getItemInHand(), 3);
            for (int x = iCuboidSelection.getMinimumPoint().getBlockX(); x <= iCuboidSelection.getMaximumPoint().getBlockX(); x++) {
                for (int z = iCuboidSelection.getMinimumPoint().getBlockZ(); z <= iCuboidSelection.getMaximumPoint().getBlockZ(); z++) {
                    if (block.getWorld().getBlockAt(x, block.getY(), z).getType() != Material.AIR) {
                        blocks.add(block.getWorld().getBlockAt(x, block.getY(), z));
                    }
                }
            }
            for (Block block1 : blocks)
                Utils.setBlockInNativeDataPalette(block1.getWorld(), block1.getX(), block1.getY(), block1.getZ(), 0, (byte) 0, true);
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
