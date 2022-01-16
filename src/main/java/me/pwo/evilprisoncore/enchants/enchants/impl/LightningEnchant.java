package me.pwo.evilprisoncore.enchants.enchants.impl;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Countable;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import me.pwo.evilprisoncore.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

@SuppressWarnings("deprecation")
public class LightningEnchant extends EvilEnchant {
    private double chance;

    public LightningEnchant(Enchants enchants) {
        super(enchants, 10);
    }

    @Override
    public void onEquip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onUnequip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (this.chance * level >= random) {
            long l1 = Time.nowMillis();
            ICuboidSelection iCuboidSelection = (ICuboidSelection) RegionUtils.getMineRegionWithHighestPriority(e.getBlock().getLocation()).getSelection();
            double totalProfit = 0.0D;
            long blocks = 0;
            int fortuneLevel = this.enchants.getEnchantsManager().getEnchantLevel(e.getPlayer().getItemInHand(), 3) + 1;
            CuboidRegion cuboidRegion = new CuboidRegion(
                    WorldEditUtil.toWEVector(iCuboidSelection.getMinimumPoint()).setX(e.getBlock().getX()).setZ(e.getBlock().getZ()),
                    WorldEditUtil.toWEVector(iCuboidSelection.getMaximumPoint()).setX(e.getBlock().getX()).setZ(e.getBlock().getZ()));
            EditSession editSession = (new EditSessionBuilder(FaweAPI.getWorld(e.getBlock().getWorld().getName()))).fastmode(true).limitUnlimited().build();
            for (Countable<BaseBlock> baseBlockCountable : editSession.getBlockDistributionWithData(cuboidRegion)) {
                if (baseBlockCountable.getID().getType() == Material.AIR.getId()) continue;
                blocks += baseBlockCountable.getAmount();
                if (this.enchants.getPlugin().getAutoSell().hasAutoSellEnabled(e.getPlayer())) {
                    totalProfit += this.enchants.getPlugin().getAutoSell().getPriceForBrokenBlock(Material.getMaterial(baseBlockCountable.getID().getType()))
                            * baseBlockCountable.getAmount();
                }
            }
            long l2 = Time.nowMillis();
            this.enchants.getPlugin().debug(String.format("Lightning::LoopBlocks >> Took %d ms.", l2 - l1));
            double money = this.enchants.getPlugin().getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), totalProfit * fortuneLevel, MultiplierType.MONEY);
            this.enchants.getPlugin().getEconomy().depositPlayer(e.getPlayer(), money);
            e.getBlock().getWorld().strikeLightningEffect(e.getBlock().getLocation());
            try {
                EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(e.getBlock().getWorld().getName()))).fastmode(true).limitUnlimited().build();
                session.setBlocks(cuboidRegion, new BaseBlock(Material.AIR.getId()));
                session.flushQueue();
            } catch (MaxChangedBlocksException maxChangedBlocksException) {
                maxChangedBlocksException.printStackTrace();
            }
            Blocks.getInstance().getApi().addBlocks(e.getPlayer(), blocks);
            long l3 = Time.nowMillis();
            this.enchants.getPlugin().debug(String.format("Lightning::Proc >> Took %d ms.", l3 - l1));
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
