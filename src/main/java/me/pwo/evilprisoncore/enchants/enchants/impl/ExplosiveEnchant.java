package me.pwo.evilprisoncore.enchants.enchants.impl;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Countable;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("deprecation")
public class ExplosiveEnchant extends EvilEnchant {
    private double chance;

    public ExplosiveEnchant(Enchants enchants) {
        super(enchants, 11);
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
            int relativeX = ThreadLocalRandom.current().nextInt(-15, 15);
            int relativeY = ThreadLocalRandom.current().nextInt(15, 35);
            int relativeZ = ThreadLocalRandom.current().nextInt(-15, 15);
            Fireball fireball = e.getBlock().getWorld().spawn(e.getBlock().getLocation().add(relativeX, relativeY, relativeZ), Fireball.class);
            fireball.setDirection(new Vector(-1 * relativeX / 2, -1 * relativeY / 2, -1 * relativeZ / 2));
            fireball.setIsIncendiary(false);
            Events.subscribe(ProjectileHitEvent.class)
                    .expireAfter(1)
                    .filter(event -> event.getEntity().equals(fireball))
                    .handler(event -> createExplosion(e.getPlayer(), e.getBlock()));
            long l2 = Time.nowMillis();
            this.enchants.getPlugin().debug(String.format("Explosive::Proc >> Took %d ms.", l2 - l1));
        }
    }

    private void createExplosion(Player player, Block block) {
        long l1 = Time.nowMillis();
        ICuboidSelection iCuboidSelection = (ICuboidSelection) RegionUtils.getMineRegionWithHighestPriority(block.getLocation()).getSelection();
        int size = 7;
        int radius = (size - 1) / 2;
        double totalProfit = 0.0D;
        long blocks = 0;
        int fortuneLevel = this.enchants.getEnchantsManager().getEnchantLevel(player.getItemInHand(), 3) + 1;
        CuboidRegion cuboidRegion = new CuboidRegion(
                new com.sk89q.worldedit.Vector(Math.max(block.getX() - radius, iCuboidSelection.getMinimumPoint().getBlockX()),
                        Math.max(block.getY() - radius, iCuboidSelection.getMinimumPoint().getBlockY()),
                        Math.max(block.getZ() - radius, iCuboidSelection.getMinimumPoint().getBlockZ())),
                new com.sk89q.worldedit.Vector(Math.min(block.getX() + radius, iCuboidSelection.getMaximumPoint().getBlockX()),
                        Math.min(block.getY() + radius, iCuboidSelection.getMaximumPoint().getBlockY()),
                        Math.min(block.getZ() + radius, iCuboidSelection.getMaximumPoint().getBlockZ())));
        EditSession editSession = (new EditSessionBuilder(FaweAPI.getWorld(block.getWorld().getName()))).fastmode(true).limitUnlimited().build();
        for (Countable<BaseBlock> baseBlockCountable : editSession.getBlockDistributionWithData(cuboidRegion)) {
            if (baseBlockCountable.getID().getType() == Material.AIR.getId()) continue;
            blocks += baseBlockCountable.getAmount();
            if (this.enchants.getPlugin().getAutoSell().hasAutoSellEnabled(player)) {
                totalProfit += this.enchants.getPlugin().getAutoSell().getPriceForBrokenBlock(Material.getMaterial(baseBlockCountable.getID().getType()))
                        * baseBlockCountable.getAmount();
            }
        }
        double money = this.enchants.getPlugin().getMultipliers().getApi().getTotalToDeposit(player, totalProfit * fortuneLevel, MultiplierType.MONEY);
        this.enchants.getPlugin().getEconomy().depositPlayer(player, money);
        try {
            EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(block.getWorld().getName()))).fastmode(true).limitUnlimited().build();
            session.setBlocks(cuboidRegion, new BaseBlock(Material.AIR.getId()));
            session.flushQueue();
        } catch (MaxChangedBlocksException maxChangedBlocksException) {
            maxChangedBlocksException.printStackTrace();
        }
        Blocks.getInstance().getApi().addBlocks(player, blocks);
        long l2 = Time.nowMillis();
        this.enchants.getPlugin().debug(String.format("Explosive::CreateExplosion >> Took %d ms.", l2 - l1));
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
