package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JumpBoostEnchant extends EvilPrisonEnchantment {
    public JumpBoostEnchant(Enchants enchants) {
        super(enchants, 5);
    }

    public void onEquip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        if (paramInt == 0) {
            onUnequip(paramPlayer, paramItemStack, paramInt);
            return;
        }
        paramPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 2147483647, paramInt - 1, true, true), true);
    }

    public void onUnequip(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        paramPlayer.removePotionEffect(PotionEffectType.JUMP);
    }

    public void onBlockBreak(BlockBreakEvent paramBlockBreakEvent, int paramInt) {}

    public void reload() {}
}
