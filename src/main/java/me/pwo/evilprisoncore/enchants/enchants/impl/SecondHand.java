package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.utils.RegionUtils;
import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.concurrent.ThreadLocalRandom;

public class SecondHand extends EvilEnchant {
    private double chance;
    private int distance;

    public SecondHand(Enchants enchants) {
        super(enchants, 13);
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
            ICuboidSelection iCuboidSelection = (ICuboidSelection) RegionUtils.getMineRegionWithHighestPriority(e.getBlock().getLocation()).getSelection();
            int randomX = ThreadLocalRandom.current().nextInt(-1 * this.distance, this.distance);
            int randomY = ThreadLocalRandom.current().nextInt(-1 * this.distance, this.distance);
            int randomZ = ThreadLocalRandom.current().nextInt(-1 * this.distance, this.distance);
            ((CraftPlayer) e.getPlayer()).getHandle().playerInteractManager.breakBlock(new BlockPosition(
                    Math.min(Math.max(e.getBlock().getX() + randomX, iCuboidSelection.getMinimumPoint().getBlockX()), iCuboidSelection.getMaximumPoint().getBlockX()),
                    Math.min(Math.max(e.getBlock().getY() + randomY, iCuboidSelection.getMinimumPoint().getBlockY()), iCuboidSelection.getMaximumPoint().getBlockY()),
                    Math.min(Math.max(e.getBlock().getZ() + randomZ, iCuboidSelection.getMinimumPoint().getBlockZ()), iCuboidSelection.getMaximumPoint().getBlockZ())));
            e.getPlayer().sendMessage("second hand enchant proced");
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
        this.distance = this.enchants.getConfig().getInt("enchants." + this.id + ".Distance", 5);
    }
}
