package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.utils.RegionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class VeinMiner extends EvilEnchant {
    private double chance;

    public VeinMiner(Enchants enchants) {
        super(enchants, 15);
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
            IWrappedRegion iWrappedRegion = RegionUtils.getMineRegionWithHighestPriority(e.getBlock().getLocation());
            int num = 20;

            Schedulers.sync().run(() -> {
                Location next = e.getBlock().getLocation().add(randomDirection());
                Location newLocation = next.add(randomDirection());
                for (int i = 0; i <= num; i++) {
                    while (!iWrappedRegion.contains(newLocation)) newLocation = next.add(randomDirection());
                    e.getBlock().getWorld().getBlockAt(newLocation).breakNaturally();
                    next = newLocation;
                    newLocation.add(randomDirection());
                }
            });
        }
    }

    private Vector randomDirection() {
        int n  = ThreadLocalRandom.current().nextInt(1, 4);
        switch (n) {
            case 1: return new Vector(1, 0, 0);
            case 2: return new Vector(0, 0, 1);
            case 3: return new Vector(-1, 0, 0);
            case 4: return new Vector(0, 0, -1);
        }
        return null;
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
    }
}
