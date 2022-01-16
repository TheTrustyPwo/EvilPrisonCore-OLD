package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.gems.Gems;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class GemFinder extends EvilEnchant {
    private double chance;
    private int min;
    private int max;

    public GemFinder(Enchants enchants) {
        super(enchants, 14);
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
            Gems.getInstance().getApi().addGems(e.getPlayer(), amount, true);
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
        this.min = this.enchants.getConfig().getInt("enchants." + id + ".Min-Gems");
        this.max = this.enchants.getConfig().getInt("enchants." + id + ".Max-Gems");
    }
}
