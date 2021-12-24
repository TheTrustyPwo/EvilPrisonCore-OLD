package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedEnchant extends EvilPrisonEnchantment {
    public SpeedEnchant(Enchants enchants) {
        super(enchants, 4);
    }

    public void onEquip(Player player, ItemStack itemStack, int level) {}

    public void onUnequip(Player player, ItemStack itemStack, int level) {
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (!e.getPlayer().hasPotionEffect(PotionEffectType.SPEED) && level > 0)
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, level - 1, true, true), true);
    }

    public void reload() {}
}
