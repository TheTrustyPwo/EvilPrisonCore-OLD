package me.pwo.evilprisoncore.autominer.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.autominer.upgrades.AutoMinerUpgrade;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class AutoMinerGUI extends Gui {
    private ItemStack pickaxe;
    private final int pickaxePlayerInventorySlot;

    public ItemStack getPickaxe() {
        return pickaxe;
    }

    public int getPickaxePlayerInventorySlot() {
        return pickaxePlayerInventorySlot;
    }

    public AutoMinerGUI(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        super(paramPlayer, 3, Text.colorize("&e&lAutoMiner"));
        this.pickaxe = paramItemStack;
        this.pickaxePlayerInventorySlot = paramInt;
    }

    @Override
    public void redraw() {
         if (isFirstDraw()) {
             ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE);
             itemStack.setDurability((short) 7);
             for (byte slot = 0; slot < getHandle().getSize(); slot++)
                 setItem(slot, ItemStackBuilder.of(itemStack).buildItem().build());
         }
         setItem(11, ItemStackBuilder.of(Material.BOOK)
                 .name("INFO")
                 .lore(Arrays.asList("as")).buildItem().build());
         for (AutoMinerUpgrade autoMinerUpgrade : AutoMinerUpgrade.all()) {
             if (!autoMinerUpgrade.isEnabled())
                 continue;
             setItem(autoMinerUpgrade.getGuiSlot(),
                     AutoMiner.getInstance().getAutoMinerManager().getGuiItem(autoMinerUpgrade, this,
                             AutoMiner.getInstance().getAutoMinerManager().getUpgradeLevel(this.pickaxe, autoMinerUpgrade.getId())));
         }
    }
}
