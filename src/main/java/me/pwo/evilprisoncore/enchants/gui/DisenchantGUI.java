package me.pwo.evilprisoncore.enchants.gui;

import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class DisenchantGUI extends Gui {
    private ItemStack pickaxe;
    private final int pickaxePlayerInventorySlot;

    public ItemStack getPickaxe() {
        return this.pickaxe;
    }

    public void setPickaxe(ItemStack paramItemStack) {
        this.pickaxe = paramItemStack;
    }

    public int getPickaxePlayerInventorySlot() {
        return this.pickaxePlayerInventorySlot;
    }

    public DisenchantGUI(Player paramPlayer, int size, ItemStack itemStack, int slot) {
        super(paramPlayer, size, Text.colorize("&8Disenchanting Menu"));
        this.pickaxe = itemStack;
        this.pickaxePlayerInventorySlot = slot;
        Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(event -> event.getInventory().equals(getHandle()))
                .handler(event -> {
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(getPlayer(), this.pickaxe);
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(getPlayer(), this.pickaxe);
                }).bindWith(this);
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        byte slot = 0;
        for (EvilEnchant enchantment : Enchants.getInstance().getEnchantsManager().getPlayerEnchants(this.pickaxe).keySet()) {
            if (!enchantment.isRefundEnabled() || !enchantment.isEnabled())
                continue;
            int i = Enchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickaxe, enchantment.getId());
            setItem(slot, Enchants.getInstance().getEnchantsManager().getRefundGuiItem(enchantment, this, i));
            slot++;
        }
    }
}
