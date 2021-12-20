package me.pwo.evilprisoncore.privatemines.gui;

import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;

public class MinesPlayerManagementGUI extends Gui {
    public MinesPlayerManagementGUI(Player player) {
        super(player, 3, Text.colorize("&8Player Management Panel"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Manage Access
        setItem(10, ItemStackBuilder.of(SkullUtils.MANAGE_ACCESS_GUI_ITEM)
                .name(Text.colorize("&e&lManage Access"))
                .lore(Arrays.asList(
                        Text.colorize("&7Grant or Revoke access to players"),
                        Text.colorize("&7Click to manage access")))
                .buildItem().bind(e -> {

                }, ClickType.LEFT).build());
        // Toggle Public Access
        String publicAccess = PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).isPublic()
                ? Text.colorize("&a&lON") : Text.colorize("&c&lOFF");
        setItem(12, ItemStackBuilder.of(SkullUtils.TOGGLE_PUBLIC_ACCESS_GUI_ITEM)
                .name(Text.colorize("&d&lPublic Access: ") + publicAccess)
                .lore(Arrays.asList(
                        Text.colorize("&7Open your Mine to the Public"),
                        Text.colorize("&7Click to toggle Public Access")))
                .buildItem().bind(e -> {
                    PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).togglePublic();
                    redraw();
                }, ClickType.LEFT).build());
        // Sales Tax
        String salesTax = Text.colorize("&7" + Utils.round(PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getTax(), 1) + "%");
        setItem(14, ItemStackBuilder.of(SkullUtils.SALES_TAX_GUI_ITEM)
                .name(Text.colorize("&6&lSales Tax: ") + salesTax)
                .lore(Arrays.asList(
                        Text.colorize("&7Control the Sales Tax in your Mine"),
                        Text.colorize("&7Click to edit the Sales Tax")))
                .buildItem().bind(e -> {
                    PlayerUtils.sendMessage(getPlayer(), "&aPlease input a new Sales Tax percentage. &7(Accepted range is 0.0-10.0%)");
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer().equals(getPlayer()))
                            .handler(event -> {
                                try {
                                    double tax = Utils.round(Double.parseDouble(event.getMessage()), 1);
                                    if (tax > 0.0D || tax <= 10.0D) {
                                        PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).setTax(tax);
                                        PlayerUtils.sendMessage(getPlayer(), "&aYou updated your mine's Sales Tax value to %value%%"
                                                .replaceAll("%value%", String.valueOf(tax)));
                                    }
                                    PlayerUtils.sendMessage(getPlayer(), "&c(!) Sales Tax must be > 0.0% and <= 10.0%");
                                } catch (NumberFormatException exception) {
                                    PlayerUtils.sendMessage(getPlayer(), "&c(!) Invalid Number");
                                }
                            }).bindWith(this);
                }, ClickType.LEFT).build());
        // Remove Player From Mine
        setItem(16, ItemStackBuilder.of(SkullUtils.REMOVE_PLAYER_GUI_ITEM)
                .name(Text.colorize("&9&lRemove Miner"))
                .lore(Arrays.asList(
                        Text.colorize("&7Click to remove a Miner from your Mine")))
                .buildItem().bind(e -> {
                    
                }, ClickType.LEFT).build());
    }
}
