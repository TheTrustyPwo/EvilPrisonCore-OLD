package me.pwo.evilprisoncore.enchants.enchants.impl;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import me.pwo.evilprisoncore.utils.RegionUtils;
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
                        if (this.enchants.getPlugin().getAutoSell().hasAutoSellEnabled(e.getPlayer())) {
                            totalProfit += this.enchants.getPlugin().getAutoSell().getPriceForBrokenBlock(block.getType());
                        }
                    }
                }
            }
            double money = this.enchants.getPlugin().getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), totalProfit * fortuneLevel, MultiplierType.MONEY);
            this.enchants.getPlugin().getEconomy().depositPlayer(e.getPlayer(), money);
            try {
                EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(block.getWorld().getName()))).fastmode(true).build();
                session.setBlocks(new CuboidRegion(
                        WorldEditUtil.toWEVector(iCuboidSelection.getMinimumPoint()).setY(block.getY()),
                        WorldEditUtil.toWEVector(iCuboidSelection.getMaximumPoint()).setY(block.getY())), new BaseBlock(Material.AIR.getId()));
                session.flushQueue();
            } catch (MaxChangedBlocksException maxChangedBlocksException) {
                maxChangedBlocksException.printStackTrace();
            }
            Blocks.getInstance().getApi().addBlocks(e.getPlayer(), blocks.size());
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
