package me.pwo.evilprisoncore.privatemines.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class MinesPublicGUI extends Gui {
    public MinesPublicGUI(Player player) {
        super(player, 6, Text.colorize("&8Public Mines"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());

    }

    /* private void getPlayerMineGuiItem(OfflinePlayer player) {
        Item item = ItemStackBuilder.of(SkullCreator.itemFromUuid(getPlayer().getUniqueId()))
                .name("&6%player%'s Mine".replaceAll("%player%", player.getName()))
                .lore(Arrays.asList(
                        " ",
                        Text.colorize("&fRank: &6%rank%".replaceAll("%rank%", String.valueOf(Ranks.getInstance().getApi().getPlayerRank(player).getId()))),
                        Text.colorize("&fTax: &6%tax%%".replaceAll("%tax%", String.valueOf(PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getTax()))),
                        " ",
                        Text.colorize("&fUnlocks"),
                        Text.colorize(" &6&l| &fBlock: &6%block%".replaceAll("%block%", PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getMaterial().name())),
                        " ",
                        Text.colorize("&fMiners: &6%miners%/%max%"),
                        Text.colorize("&fMine Size: &6%size% x %size%".replaceAll("%size%", String.valueOf(PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getMineSize()))),
                        " ",
                        Text.colorize("&a&lLEFT-CLICK &ato join this mine")
                )).buildItem().bind(e -> {
                    close();

                }, ClickType.LEFT).build();
    } */
}
