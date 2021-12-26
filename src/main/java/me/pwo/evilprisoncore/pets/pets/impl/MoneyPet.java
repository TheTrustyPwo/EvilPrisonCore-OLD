package me.pwo.evilprisoncore.pets.pets.impl;

import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pets.model.PetTier;
import me.pwo.evilprisoncore.pets.pets.EvilPet;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MoneyPet extends EvilPet {
    public MoneyPet(Pets pets) {
        super(pets, 1);
    }

    @Override
    public void onEnable(Player player, ItemStack itemStack, int level) {
        player.sendMessage("given multi: " + level * 0.1);
    }

    @Override
    public void onDisable(Player player, ItemStack itemStack, int level) {
        player.sendMessage("removed multi: " + level * 0.1);
    }

    @Override
    public void onLevelUp(Player player, ItemStack itemStack, int level) {
        player.sendMessage("given multi: 0.1");
    }

    @Override
    public void onTierUp(Player player, ItemStack itemStack, PetTier tier) {
        player.sendMessage("set multi: 0.1");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {

    }

    @Override
    public void reload() {

    }
}
