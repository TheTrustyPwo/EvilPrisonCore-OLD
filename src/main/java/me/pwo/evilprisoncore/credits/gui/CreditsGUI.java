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
        super(player, 3, Text.colorize("&8Credits"));
    }
    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Season Age Info
        setItem(10, ItemStackBuilder.of(Material.BOOK)
                .name("&6Season Age: &e" + Credits.getInstance().getCreditsManager().getSeasonAge())
                .lore(Arrays.asList(
                        "&7OP Prison Season II released on",
                        "&f9 Jan 2022 &7and will end on &f20 Feb 2022&7."
                )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
        // Your Credits Balance
        setItem(13, ItemStackBuilder.of(SkullUtils.getPlayerHead(getPlayer()))
                .name("&6Your Credits: &4☀&c%credits%".replaceAll("%credits%", String.valueOf(Credits.getInstance().getCreditsManager().getPlayerCredits(getPlayer()))))
                .lore(Arrays.asList(
                        "&7Credits can be exchanged for store credit!",
                        "&7Type &f/credits withdraw <amount>",
                        "&7to exchange them! You have withdraw to",
                        "&7a minimum of &f100,000 &7credits."
                )).buildItem().build());
        // Credits Multiplier
        setItem(16, ItemStackBuilder.of(Material.BOOK)
                .name("&6Rate: &e$1 = &4☀&c%rate% &6(&ex%multi%&6)"
                        .replaceAll("%rate%", String.valueOf(Credits.getInstance().getCreditsManager().getCreditsExchangeRate()))
                        .replaceAll("%multi%", String.valueOf(Credits.getInstance().getCreditsManager().getPayoutMultiplier())))
                .lore(Arrays.asList(
                        "&7As the season ages, the credits",
                        "&7multiplier will increase! This means you will be",
                        "&7able to trade credits for more store",
                        "&7credit during late season. It will",
                        "&7get as high as &fx10.50 &7at the",
                        "&7end of the season!"
                )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
        // Help Item
        setItem(22, ItemStackBuilder.of(Material.BOOK_AND_QUILL)
                .name("&eInformation")
                .lore(Arrays.asList(
                        "&6What are credits?",
                        "&7Credits are a custom currency on our server",
                        "&7which you can exchange for store credit to buy",
                        "&7Ranks, Crates, Special items and abilities etc...",
                        " ",
                        "&6How to earn credits?",
                        "&7Now, here's the exciting part: ANYONE can earn credits!",
                        "&7There are multiple ways of earning credits, such as",
                        "&7Mining, AutoMining, Ranks, Robots, Events, PvP and more!",
                        "&7This makes it extremely fair for ALL players as",
                        "&7you can't just P2W your way for credits.",
                        " ",
                        "&6What to do with credits",
                        "&7Type &f/credits withdraw <amount> &7to exchange them!",
                        "&7Currently, you can only exchange them into gift cards, but some",
                        "&7day in the future, when our server gets big enough,",
                        "&7you guys will even be able to withdraw them into Paypal!"
                )).enchant(Enchantment.DURABILITY).flag(ItemFlag.HIDE_ENCHANTS).buildItem().build());
    }
}
