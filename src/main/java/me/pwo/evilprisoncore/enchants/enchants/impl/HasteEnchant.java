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

    public void onEquip(Player player, ItemStack itemStack, int level) {}

    public void onUnequip(Player player, ItemStack itemStack, int level) {
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
    }

    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (!e.getPlayer().hasPotionEffect(PotionEffectType.FAST_DIGGING) && level > 0)
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 15, level - 1, true, true), true);
    }


    public void reload() {}
}
