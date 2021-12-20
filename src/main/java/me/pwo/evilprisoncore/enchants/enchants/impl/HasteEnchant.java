package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HasteEnchant extends EvilPrisonEnchantment {
    public HasteEnchant(Enchants enchants) {
        super(enchants, 3);
    }

    public void onEquip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        if (paramInt == 0) {
            onUnequip(paramPlayer, paramItemStack, paramInt);
            return;
        }
        paramPlayer.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, paramInt - 1, true, true), true);
    }

    public void onUnequip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        paramPlayer.removePotionEffect(PotionEffectType.FAST_DIGGING);
    }

    public void onBlockBreak(BlockBreakEvent paramBlockBreakEvent, int paramInt) {}

    public void reload() {}
}
