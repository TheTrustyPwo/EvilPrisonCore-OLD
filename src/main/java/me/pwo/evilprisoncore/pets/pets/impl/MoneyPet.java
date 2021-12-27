package me.pwo.evilprisoncore.pets.pets.impl;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pets.model.PetTier;
import me.pwo.evilprisoncore.pets.pets.EvilPet;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class MoneyPet extends EvilPet {
    private final Pets pets;

    public MoneyPet(Pets pets) {
        super(pets, 1);
        this.pets = pets;
    }

    @Override
    public void onEnable(Player player, ItemStack itemStack, int level) {
        this.pets.getPlugin().getMultiplier().getApi().addPlayerMultiplier(player, level * 0.1, TimeUnit.DAYS, Integer.MAX_VALUE, MultiplierType.MONEY);
    }

    @Override
    public void onDisable(Player player, ItemStack itemStack, int level) {
        this.pets.getPlugin().getMultiplier().getApi().removePlayerMultiplier(player, level * 0.1, TimeUnit.DAYS, Integer.MAX_VALUE, MultiplierType.MONEY);
    }

    @Override
    public void onLevelUp(Player player, ItemStack itemStack, int level) {
        this.pets.getPlugin().getMultiplier().getApi().addPlayerMultiplier(player, 0.1, TimeUnit.DAYS, Integer.MAX_VALUE, MultiplierType.MONEY);
    }

    @Override
    public void onTierUp(Player player, ItemStack itemStack, PetTier tier) {
        this.pets.getPlugin().getMultiplier().getApi().removePlayerMultiplier(player, (tier.getMaxLevel() - 1) * 0.1, TimeUnit.DAYS, Integer.MAX_VALUE, MultiplierType.MONEY);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {

    }

    @Override
    public void reload() {

    }
}
