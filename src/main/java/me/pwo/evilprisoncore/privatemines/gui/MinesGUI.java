package me.pwo.evilprisoncore.privatemines.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class MinesGUI extends Gui {
    public MinesGUI(Player player) {
        super(player, 4, Text.colorize("&8Mine Menu"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Player Head Item
        setItem(4, ItemStackBuilder.of(SkullCreator.itemFromUuid(getPlayer().getUniqueId()))
                .name(Text.colorize(String.format("&6&l%s's Mine", getPlayer().getName())))
                .buildItem().build());
        // View Public Mines
        setItem(20, ItemStackBuilder.of(SkullUtils.VIEW_PUBLIC_MINE_GUI_ITEM)
                .name(Text.colorize("&6&lPublic Mines"))
                .lore(Arrays.asList("&7Click to view available Public Mines"))
                .buildItem().bind(e -> {
                    (new MinesPublicGUI(getPlayer())).open();
                }, ClickType.LEFT).build());
        // Player Management
        setItem(21, ItemStackBuilder.of(SkullUtils.PLAYER_MANAGEMENT_GUI_ITEM)
                .name(Text.colorize("&e&lPlayer Management"))
                .lore(Arrays.asList(Text.colorize("&7Click tov iew the Player Management Panel")))
                .buildItem().bind(e -> {
                    (new MinesPlayerManagementGUI(getPlayer())).open();
                }, ClickType.LEFT).build());
        // Teleport To Mine
        /* setItem(22, ItemStackBuilder.of(SkullUtils.TELEPORT_TO_MINE_GUI_ITEM)
                .name(Text.colorize("&3&lTeleport to Mine"))
                .lore(Arrays.asList(Text.colorize("&7Click to teleport to your Mine")))
                .buildItem().bind(e -> {
                    close();
                    Location location = PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).getSpawnLocation();
                    if (location != null) {
                        getPlayer().teleport(location);
                    }
                }, ClickType.LEFT).build());
        // Reset Mine
        setItem(23, ItemStackBuilder.of(SkullUtils.RESET_ITEM_GUI_ITEM)
                .name(Text.colorize("&9&lReset Mine"))
                .lore(Arrays.asList(Text.colorize("&7Click to reset your Mine")))
                .buildItem().bind(e -> {
                    close();
                    PrivateMines.getInstance().getMines().get(getPlayer().getUniqueId()).reset();
                    PlayerUtils.sendMessage(getPlayer(), "&aYour Mine has been reset!");
                }, ClickType.LEFT).build()); */
        // Manage Blocks
        setItem(24, ItemStackBuilder.of(SkullUtils.MANAGE_BLOCKS_GUI_ITEM)
                .name(Text.colorize("&5&lManage Mine Blocks"))
                .lore(Arrays.asList(Text.colorize("&7Click to change the blocks in the mine")))
                .buildItem().bind(e -> {
                    close();
                    (new MinesManageBlocksGUI(getPlayer())).open();
                }, ClickType.LEFT).build());
    }
}
