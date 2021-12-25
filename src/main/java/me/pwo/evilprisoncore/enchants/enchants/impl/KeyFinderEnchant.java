package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KeyFinderEnchant extends EvilEnchant {
    private double chance;
    private List<String> commands;

    public KeyFinderEnchant(Enchants enchants) {
        super(enchants, 8);
    }

    @Override
    public void onEquip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onUnequip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (this.chance * level >= random)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    this.commands.get(ThreadLocalRandom.current().nextInt(this.commands.size()))
                            .replace("%player%", e.getPlayer().getName()));
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
        this.commands = this.enchants.getConfig().getStringList("enchants." + this.id + ".Commands");
    }
}
