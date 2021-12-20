package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EfficiencyEnchant extends EvilPrisonEnchantment {
    public EfficiencyEnchant(Enchants enchants) {
        super(enchants, 1);
    }

    public void onEquip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        ItemMeta itemMeta = paramItemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, paramInt, true);
        paramItemStack.setItemMeta(itemMeta);
    }

    public void onUnequip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {}

    public void onBlockBreak(BlockBreakEvent paramBlockBreakEvent, int paramInt) {}

    public void reload() {}
}
