package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.utils.RegionUtils;
import me.pwo.evilprisoncore.utils.Utils;
import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ShockwaveEnchant extends EvilEnchant {
    private double chance;

    public ShockwaveEnchant(Enchants enchants) {
        super(enchants, 7);
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
            for (Block block1 : blocks) {
                Utils.setBlockInNativeDataPalette(block1.getWorld(), block1.getX(), block1.getY(), block1.getZ(), 0, (byte) 0, true);
            }
            Blocks.getInstance().getApi().addBlocks(e.getPlayer(), blocks.size());
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
