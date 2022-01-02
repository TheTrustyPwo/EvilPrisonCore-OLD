package me.pwo.evilprisoncore.privatemines.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MinesPublicGUI extends Gui {
    private static final MenuScheme MINES_DISPLAY = new MenuScheme()
            .mask("000000000")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("000000000");
    private final int page;

    public MinesPublicGUI(Player player, int page) {
        super(player, 6, Text.colorize("&8Public Mines"));
        this.page = page;
    }

    @Override
    public void redraw() {
        List<Mine> publicMines = PrivateMines.getInstance().getPrivateMinesManager().getAllMines()
                .values().stream().filter(Mine::isPublic)
                .sorted(Comparator.comparing(mine -> Ranks.getInstance().getApi().getPlayerRank(Players.getOfflineNullable(mine.getOwner())).getId()))
                .collect(Collectors.toList());
        int totalPages = (int) Math.floor((float) publicMines.size() / 28) + 1;
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Prev Page / Back
        if (this.page == 1) {
            setItem(0, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                    .name("&6Go Back")
                    .build(() -> new MinesGUI(getPlayer()).open()));
        } else {
            setItem(0, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                    .name("&e<- (%previous%/%max%)"
                            .replaceAll("%previous%", String.valueOf(this.page - 1))
                            .replaceAll("%max%", String.valueOf(totalPages)))
                    .build(() -> (new MinesPublicGUI(getPlayer(), this.page - 1)).open()));
        }
        // Next Page
        if (this.page == totalPages) {
            setItem(8, ItemStackBuilder.of(SkullUtils.FORWARD_BUTTON.clone())
                    .name("&7Last Page").buildItem().build());
        } else {
            setItem(8, ItemStackBuilder.of(SkullUtils.FORWARD_BUTTON.clone())
                    .name("&e(%next%/%max%) ->"
                            .replaceAll("%next%", String.valueOf(this.page + 1))
                            .replaceAll("%max%", String.valueOf(totalPages)))
                    .build(() -> (new MinesPublicGUI(getPlayer(), this.page + 1)).open()));
        }
        // Refresh
        setItem(5, ItemStackBuilder.of(SkullUtils.REFRESH.clone())
                .name("&6&lRefresh").lore("&7Click to refresh").build(this::redraw));
        // Your Mine
        Mine mine = PrivateMines.getInstance().getPrivateMinesManager().getPlayerMine(getPlayer().getUniqueId());
        if (mine == null) setItem(4, ItemStackBuilder.of(SkullCreator.itemFromUuid(getPlayer().getUniqueId()))
                .name("&c&lNO MINE").buildItem().build());
        else setItem(4, getPlayerMineGuiItem(mine));

        MenuPopulator populator = MINES_DISPLAY.newPopulator(this);
        int i = (page - 1) * 28;
        while (populator.hasSpace() && (i + 1) <= publicMines.size()) {
            populator.accept(getPlayerMineGuiItem(publicMines.get(i)));
            i++;
        }
    }

    private Item getPlayerMineGuiItem(Mine mine) {
        OfflinePlayer player = Players.getOfflineNullable(mine.getOwner());
        return ItemStackBuilder.of(SkullCreator.itemFromUuid(player.getUniqueId()))
                .name("&6%player%'s Mine".replaceAll("%player%", player.getName()))
                .lore(
                        " ",
                        "&fRank: &6%rank%".replaceAll("%rank%", String.valueOf(Ranks.getInstance().getApi().getPlayerRank(player).getId())),
                        "&fTax: &6%tax%%".replaceAll("%tax%", Utils.formatNumber(mine.getTax())),
                        " ",
                        "&fInfo",
                        " &6&l│ &fBlock: &6%block%".replaceAll("%block%", mine.getMaterial().name()),
                        " &6&l│ &fMiners: &6%miners%/10".replaceAll("%miners%", String.valueOf(mine.getPlayersInMine().size())),
                        " &6&l│ &fMine Size: &6%size% x %size%".replaceAll("%size%", String.valueOf(mine.getMineSize())),
                        " ",
                        "&a&lLEFT-CLICK &ato join this mine"
                ).build(() -> {
                    close();
                    mine.teleport(getPlayer());
                });
    }
}
