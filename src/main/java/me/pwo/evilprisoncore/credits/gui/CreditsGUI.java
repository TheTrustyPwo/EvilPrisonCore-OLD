package me.pwo.evilprisoncore.credits.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class CreditsGUI extends Gui {
    public CreditsGUI(Player player) {
        super(player, 4, Text.colorize("&8Credits"));
    }
    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Season Age Info
        setItem(10, ItemStackBuilder.of(Material.BOOK)
                .name("&6&lSeason Age: &e&l" + Credits.getInstance().getCreditsManager().getSeasonAge())
                .lore(Arrays.asList(

                )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
        // Your Credits Balance
        setItem(13, ItemStackBuilder.of(SkullUtils.getPlayerHead(getPlayer()))
                .name("&6&lYour Credits: &4☀&c%credits%".replaceAll("%credits%", String.valueOf(Credits.getInstance().getCreditsManager().getPlayerCredits(getPlayer()))))
                .lore(Arrays.asList(

                )).buildItem().build());
        // Credits Multiplier
        setItem(16, ItemStackBuilder.of(Material.BOOK)
                .name("&6&lCredits Multi: &e&lx" + Credits.getInstance().getCreditsManager().getPayoutMultiplier())
                .lore(Arrays.asList(

                        )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
        // Help Item
        setItem(31, ItemStackBuilder.of(Material.BOOK_AND_QUILL)
                .name("&eHow to get &4☀&c%Credits&e?")
                .lore(Arrays.asList(

                )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
    }
}
