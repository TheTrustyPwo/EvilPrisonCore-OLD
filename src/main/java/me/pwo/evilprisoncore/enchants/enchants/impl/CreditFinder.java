package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CreditFinder extends EvilEnchant {
    private double chance;
    private int min;
    private int max;

    public CreditFinder(Enchants enchants) {
        super(enchants, 12);
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
            int amount = ThreadLocalRandom.current().nextInt(this.min, this.max);
            Credits.getInstance().getApi().addCredits(e.getPlayer(), amount);
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + id + ".Chance");
        this.min = this.enchants.getConfig().getInt("enchants." + id + ".Min-Credits");
        this.max = this.enchants.getConfig().getInt("enchants." + id + ".Max-Credits");
    }
}
