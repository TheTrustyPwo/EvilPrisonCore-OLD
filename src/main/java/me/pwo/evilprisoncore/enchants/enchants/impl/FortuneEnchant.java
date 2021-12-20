package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FortuneEnchant extends EvilPrisonEnchantment {
    public FortuneEnchant(Enchants enchants) {
        super(enchants, 2);
    }

    public void onEquip(Player player, ItemStack itemStack, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, level, true);
        itemStack.setItemMeta(itemMeta);
    }

    public void onUnequip(Player player, ItemStack itemStack, int level) {}

    public void onBlockBreak(BlockBreakEvent e, int level, double random) {}

    public void reload() {}
}
