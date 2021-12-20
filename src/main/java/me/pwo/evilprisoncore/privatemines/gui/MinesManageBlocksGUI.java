package me.pwo.evilprisoncore.privatemines.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class MinesManageBlocksGUI extends Gui {
    public MinesManageBlocksGUI(Player player) {
        super(player, 6, Text.colorize("&8Manage Blocks"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < 9; slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Back Button
        setItem(0, ItemStackBuilder.of(SkullUtils.BACK_BUTTON)
                .name(Text.colorize("&6Go Back"))
                .buildItem().bind(e -> {
                    close();
                    (new MinesGUI(getPlayer())).open();
                }, ClickType.LEFT).build());
        int slot = 9;
        for (Material material : Ranks.getInstance().getRankManager().getUnlockedBlocks(Ranks.getInstance().getApi().getPlayerRank(getPlayer()).getId())) {
            if (PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getMaterial().equals(material)) {
                setItem(slot, ItemStackBuilder.of(material)
                        .name(Text.colorize("&6") + material.name())
                        .lore(Arrays.asList(
                                " ",
                                Text.colorize("&fStatus: &aEnabled")
                        )).buildItem().build());
            } else {
                setItem(slot, ItemStackBuilder.of(material)
                        .name(Text.colorize("&6") + material.name())
                        .lore(Arrays.asList(
                                " ",
                                Text.colorize("&fStatus: &cDisabled"),
                                " ",
                                Text.colorize("&a&lLEFT-CLICK &ato select block")
                        )).buildItem().bind(e -> {
                            PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).setMaterial(material);
                            redraw();
                        }, ClickType.LEFT).build());
            }
            slot++;
        }
    }
}
