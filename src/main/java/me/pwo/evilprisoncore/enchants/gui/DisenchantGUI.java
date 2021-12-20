package me.pwo.evilprisoncore.enchants.gui;

import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class DisenchantGUI extends Gui {
    private static String GUI_TITLE;
    private static Item EMPTY_SLOT_ITEM;
    private static Item HELP_ITEM;
    private static int HELP_ITEM_SLOT;
    private static int PICKAXE_ITEM_SLOT;
    private static int GUI_LINES;
    private static boolean PICKAXE_ITEM_ENABLED;
    private static boolean HELP_ITEM_ENABLED;
    private ItemStack pickAxe;
    private int pickaxePlayerInventorySlot;

    static {
        reload();
    }

    public ItemStack getPickAxe() {
        return this.pickAxe;
    }

    public void setPickAxe(ItemStack paramItemStack) {
        this.pickAxe = paramItemStack;
    }

    public int getPickaxePlayerInventorySlot() {
        return this.pickaxePlayerInventorySlot;
    }

    public DisenchantGUI(Player paramPlayer, ItemStack paramItemStack, int paramInt) {
        super(paramPlayer, GUI_LINES, GUI_TITLE);
        this.pickAxe = paramItemStack;
        this.pickaxePlayerInventorySlot = paramInt;
        Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(paramInventoryCloseEvent -> paramInventoryCloseEvent.getInventory().equals(getHandle()))
                .handler(paramInventoryCloseEvent -> {
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(getPlayer(), this.pickAxe);
                    EvilPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(getPlayer(), this.pickAxe);
                }).bindWith(this);
    }

    public void redraw() {
        if (isFirstDraw())
            for (byte b = 0; b < getHandle().getSize(); b++)
                setItem(b, EMPTY_SLOT_ITEM);
        if (HELP_ITEM_ENABLED)
            setItem(HELP_ITEM_SLOT, HELP_ITEM);
        if (PICKAXE_ITEM_ENABLED)
            setItem(PICKAXE_ITEM_SLOT, Item.builder(this.pickAxe).build());
        for (EvilPrisonEnchantment ultraPrisonEnchantment : EvilPrisonEnchantment.all()) {
            if (!ultraPrisonEnchantment.isRefundEnabled() || !ultraPrisonEnchantment.isEnabled())
                continue;
            int i = Enchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, ultraPrisonEnchantment.getId());
            setItem(ultraPrisonEnchantment.refundGuiSlot(), Enchants.getInstance().getEnchantsManager().getRefundGuiItem(ultraPrisonEnchantment, this, i));
        }
    }

    public static void reload() {
        GUI_TITLE = Text.colorize(Enchants.getInstance().getConfig().getString("disenchant_menu.title"));
        GUI_LINES = Enchants.getInstance().getConfig().getInt("disenchant_menu.lines");
        EMPTY_SLOT_ITEM = ItemStackBuilder.of(Material.getMaterial(Enchants.getInstance().getConfig().getString("disenchant_menu.empty_slots"))).buildItem().build();
        HELP_ITEM_ENABLED = Enchants.getInstance().getConfig().getBoolean("disenchant_menu.help_item.enabled", true);
        PICKAXE_ITEM_ENABLED = Enchants.getInstance().getConfig().getBoolean("disenchant_menu.pickaxe_enabled", true);
        if (HELP_ITEM_ENABLED) {
            String str = Enchants.getInstance().getConfig().getString("disenchant_menu.help_item.Base64", null);
            if (str != null) {
                HELP_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(str))
                        .name(Enchants.getInstance().getConfig().getString("disenchant_menu.help_item.name"))
                        .lore(Enchants.getInstance().getConfig().getStringList("disenchant_menu.help_item.lore")).buildItem().build();
            } else {
                HELP_ITEM = ItemStackBuilder.of(Material.getMaterial(Enchants.getInstance().getConfig().getString("disenchant_menu.help_item.material")))
                        .name(Enchants.getInstance().getConfig().getString("disenchant_menu.help_item.name"))
                        .lore(Enchants.getInstance().getConfig().getStringList("disenchant_menu.help_item.lore")).buildItem().build();
            }
            HELP_ITEM_SLOT = Enchants.getInstance().getConfig().getInt("disenchant_menu.help_item.slot");
        }
        if (PICKAXE_ITEM_ENABLED)
            PICKAXE_ITEM_SLOT = Enchants.getInstance().getConfig().getInt("disenchant_menu.pickaxe_slot");
    }
}
