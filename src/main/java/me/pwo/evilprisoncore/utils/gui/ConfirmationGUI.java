package me.pwo.evilprisoncore.utils.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ConfirmationGUI extends Gui {
    private static final ItemStack CONFIRM = ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(5).name("&aConfirm").build();
    private static final ItemStack CANCEL = ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(14).name("&cCancel").build();

    public ConfirmationGUI(Player player, String title) {
        super(player, 1, title);
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
            setItem(3, ItemStackBuilder.of(CONFIRM).build(this::confirm));
            setItem(5, ItemStackBuilder.of(CANCEL).build(this::close));
        }
    }

    public abstract void confirm();
}
