package me.pwo.evilprisoncore.pickaxe.pickaxelevels.api;

import me.pwo.evilprisoncore.pickaxe.pickaxelevels.PickaxeLevels;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PickaxeLevelsAPIImpl implements PickaxeLevelsAPI {
    private PickaxeLevels pickaxeLevels;

    public PickaxeLevelsAPIImpl(PickaxeLevels pickaxeLevels) {
        this.pickaxeLevels = pickaxeLevels;
    }

    public PickaxeLevel getPickaxeLevel(ItemStack paramItemStack) {
        return this.pickaxeLevels.getPickaxeLevel(paramItemStack);
    }

    public PickaxeLevel getPickaxeLevel(Player paramPlayer) {
        ItemStack itemStack = this.pickaxeLevels.findPickaxe(paramPlayer);
        return getPickaxeLevel(itemStack);
    }

    public void setPickaxeLevel(Player paramPlayer, ItemStack paramItemStack, PickaxeLevel paramPickaxeLevel) {
        this.pickaxeLevels.setPickaxeLevel(paramItemStack, paramPickaxeLevel);
    }
}
