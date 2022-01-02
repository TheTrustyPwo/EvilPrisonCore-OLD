package me.pwo.evilprisoncore.privatemines.gui;

import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
        Mine mine = PrivateMines.getInstance().getPrivateMinesManager().getPlayerMine(getPlayer().getUniqueId());
        setItem(10, ItemStackBuilder.of(SkullUtils.MANAGE_ACCESS_GUI_ITEM.clone())
                .name(Text.colorize("&e&lManage Access"))
                .lore("&7Grant or Revoke access to players", "&7Click to manage access")
                .buildItem().bind(e -> (new MinesManageAccessGUI(getPlayer())).open(), ClickType.LEFT).build());
        // Toggle Public Access
        setItem(12, ItemStackBuilder.of(SkullUtils.TOGGLE_PUBLIC_ACCESS_GUI_ITEM.clone())
                .name("&d&lPublic Access: %toggled%".replaceAll("%toggled%", mine.isPublic() ? "&a&lON" : "&c&lOFF"))
                .lore("&7Open your Mine to the Public", "&7Click to toggle Public Access")
                .buildItem().bind(e -> {
                    mine.setPublic(!mine.isPublic());
                    redraw();
                }, ClickType.LEFT).build());
        // Sales Tax
        setItem(14, ItemStackBuilder.of(SkullUtils.SALES_TAX_GUI_ITEM.clone())
                .name("&6&lSales Tax: &7%tax%%".replaceAll("%tax%", Utils.formatNumber(mine.getTax())))
                .lore("&7Control the Sales Tax in your Mine", "&7Click to edit the Sales Tax")
                .buildItem().bind(e -> {
                    close();
                    PlayerUtils.sendMessage(getPlayer(), "&aPlease input a new Sales Tax percentage. &7(Accepted range is 0.0%-10.0%)");
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer().equals(getPlayer()))
                            .handler(event -> {
                                event.setCancelled(true);
                                try {
                                    double tax = Utils.round(Double.parseDouble(event.getMessage()), 1);
                                    if (tax >= 0.0D && tax <= 10.0D) {
                                        mine.setTax(tax);
                                        PlayerUtils.sendMessage(getPlayer(), "&aYou updated your mine's Sales Tax value to %value%%"
                                                .replaceAll("%value%", String.valueOf(tax)));
                                        return;
                                    }
                                    PlayerUtils.sendMessage(getPlayer(), "&c&l(!) &cSales Tax must be >= 0.0% and <= 10.0%");
                                } catch (NumberFormatException exception) {
                                    PlayerUtils.sendMessage(getPlayer(), "&c&l(!) &cInvalid Number");
                                }
                            });
                }, ClickType.LEFT).build());
        // Remove Player From Mine
        setItem(16, ItemStackBuilder.of(SkullUtils.REMOVE_PLAYER_GUI_ITEM.clone())
                .name(Text.colorize("&9&lBan player"))
                .lore("&7Click to ban a player from your Mine")
                .buildItem().bind(e -> {
                    close();
                    PlayerUtils.sendMessage(getPlayer(), "&aPlease specify the player to ban.");
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer().equals(getPlayer()))
                            .handler(event -> {
                                event.setCancelled(true);
                                mine.ban(Players.getOfflineNullable(event.getMessage()));
                            });
                }, ClickType.LEFT).build());
    }
}
