package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EfficiencyEnchant extends EvilEnchant {
    public EfficiencyEnchant(Enchants enchants) {
        super(enchants, 1);
    }

    public void onEquip(Player player, ItemStack itemStack, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, level, true);
        itemStack.setItemMeta(itemMeta);
    }

    public void onUnequip(Player player, ItemStack itemStack, int level) {}

    public void onBlockBreak(BlockBreakEvent e, int level, double random) {}

    public void reload() {}
}
