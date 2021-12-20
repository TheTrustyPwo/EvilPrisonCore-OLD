package me.pwo.evilprisoncore.menu.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.menu.Menu;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class PrisonMenuGUI extends Gui {
    public PrisonMenuGUI(Player player) {
        super(player, 6, Text.colorize("&6&lPrison Menu"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Server Links
        setItem(48, ItemStackBuilder.of(SkullUtils.SERVER_LINKS_GUI_ITEM)
                .name(Text.colorize("&9&lServer Links"))
                .lore(Arrays.asList(
                        Text.colorize("&bDiscord: &fevilkingdom.net/discord"),
                        Text.colorize("&dApply: &fevilkingdom.net/apply"),
                        Text.colorize("&6Store: &fstore.evilkingdom.net"),
                        Text.colorize("&aBans: &fevilkingdom.net/punishments")
                )).buildItem().bind(e -> {

                }, ClickType.LEFT).build());
        // Player Profile Item
        setItem(49, ItemStackBuilder.of(SkullUtils.getPlayerHead(getPlayer()))
                .name(Text.colorize("&c&lYour Profile"))
                .lore(Arrays.asList(
                        Text.colorize("&7Click to view your profile."),
                        Text.colorize("&7/user <player>")
                )).buildItem().bind(e -> {

                }, ClickType.LEFT).build());
        // Vote Button
        setItem(50, ItemStackBuilder.of(SkullUtils.VOTE_BUTTON_GUI_ITEM)
                .name(Text.colorize("&6&lVote"))
                .lore(Arrays.asList(Text.colorize("&7Click to vote for the server")))
                .buildItem().bind(e -> {

                }, ClickType.LEFT).build());
        // Settings Button
        setItem(51, ItemStackBuilder.of(SkullUtils.SETTINGS_BUTTON_GUI_ITEM)
                .name(Text.colorize("&4&lSettings"))
                .lore(Arrays.asList(Text.colorize("&7Click to manage your settings.")))
                .buildItem().bind(e -> {

                }, ClickType.LEFT).build());
        // Toggle Menu Button
        setItem(53, ItemStackBuilder.of(Material.REDSTONE_TORCH_ON)
                .name(Text.colorize("&6/togglemenu"))
                .lore(Arrays.asList(Text.colorize("&7Click to disable the menu item")))
                .buildItem().bind(e -> {
                    close();
                    Menu.getInstance().toggleMenu(getPlayer());
                }, ClickType.LEFT).build());
    }
}
