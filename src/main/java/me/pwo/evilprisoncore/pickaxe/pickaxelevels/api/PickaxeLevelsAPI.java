package me.pwo.evilprisoncore.pickaxe.pickaxelevels.api;

import me.pwo.evilprisoncore.pickaxe.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PickaxeLevelsAPI {
    PickaxeLevel getPickaxeLevel(ItemStack paramItemStack);

    PickaxeLevel getPickaxeLevel(Player paramPlayer);

    void setPickaxeLevel(Player paramPlayer, ItemStack paramItemStack, PickaxeLevel paramPickaxeLevel);
}
