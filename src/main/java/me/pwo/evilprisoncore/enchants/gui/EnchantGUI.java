package me.pwo.evilprisoncore.enchants.gui;

import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantGUI extends Gui {
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

    public EnchantGUI(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        super(paramPlayer, 6, Text.colorize("&8Enchants Menu"));
        this.pickaxe = paramItemStack;
        this.pickaxePlayerInventorySlot = paramInt;
        Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(paramInventoryCloseEvent -> paramInventoryCloseEvent.getInventory().equals(getHandle()))
                .handler(paramInventoryCloseEvent -> {
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(getPlayer(), this.pickaxe);
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(getPlayer(), this.pickaxe);
                }).bindWith(this);
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Disenchant
        setItem(11, ItemStackBuilder.of(Material.ANVIL)
                .name("&c&lDisenchanter")
                .lore(
                        "&7Remove enchants which you do not want",
                        "&7and get your tokens refunded!",
                        " ",
                        "&a&lLEFT-CLICK &ato disenchant").build(() -> {
                close();
                (new DisenchantGUI(getPlayer(),
                        (int) Math.ceil((float) Enchants.getInstance().getEnchantsManager().getPlayerEnchants(this.pickaxe).size() / 9),
                        this.pickaxe, this.pickaxePlayerInventorySlot)).open();
        }));
        // Pickaxe
        setItem(13, Item.builder(this.pickaxe).build());
        for (EvilEnchant enchantment : EvilEnchant.all()) {
            if (!enchantment.isEnabled())
                continue;
            int i = Enchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickaxe, enchantment.getId());
            setItem(enchantment.getGuiSlot(), Enchants.getInstance().getEnchantsManager().getGuiItem(enchantment, this, i));
        }
    }
}
