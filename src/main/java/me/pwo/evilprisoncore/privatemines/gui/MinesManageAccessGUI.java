package me.pwo.evilprisoncore.privatemines.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MinesManageAccessGUI extends Gui {
    private static final MenuScheme PLAYER_ICONS = new MenuScheme()
            .mask("000000000")
            .mask("111111111")
            .mask("000000000");
    private static final MenuScheme PLAYER_BUTTONS = new MenuScheme()
            .mask("000000000")
            .mask("000000000")
            .mask("111111111");

    public MinesManageAccessGUI(Player player) {
        super(player, 3, Text.colorize("&8Access Management"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        Mine mine = PrivateMines.getInstance().getPrivateMinesManager().getPlayerMine(getPlayer().getUniqueId());
        // Back Button
        setItem(0, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                .name(Text.colorize("&6Go Back"))
                .buildItem().bind(e -> (new MinesPlayerManagementGUI(getPlayer())).open(), ClickType.LEFT).build());
        MenuPopulator playerIconsPopulator = PLAYER_ICONS.newPopulator(this);
        MenuPopulator playerButtonsPopulator = PLAYER_BUTTONS.newPopulator(this);
        for (UUID uuid : mine.getWhitelistedPlayers()) {
            playerIconsPopulator.accept(ItemStackBuilder.of(SkullCreator.itemFromUuid(uuid))
                    .name("&e&l%player%".replaceAll("%player%", Players.getOfflineNullable(uuid) == null ? "&cUnknown Player &4(Contact an Admin!)" : Players.getOfflineNullable(uuid).getName()))
                    .buildItem().build());
            playerButtonsPopulator.accept(ItemStackBuilder.of(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14))
                    .name("&c&lREMOVE PLAYER")
                    .build(() -> {
                        mine.unwhitelist(Players.getOfflineNullable(uuid));
                        redraw();
                    }));
        }
        while (playerIconsPopulator.hasSpace() && playerButtonsPopulator.hasSpace()) {
            playerIconsPopulator.accept(ItemStackBuilder.of(SkullUtils.QUESTION_MARK.clone())
                    .name("&7&lEMPTY SLOT").buildItem().build());
            playerButtonsPopulator.accept(ItemStackBuilder.of(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5))
                    .name("&a&lADD PLAYER")
                    .build(() -> {
                        close();
                        PlayerUtils.sendMessage(getPlayer(), "&aPlease specify the player to whitelist.");
                        Events.subscribe(AsyncPlayerChatEvent.class)
                                .expireAfter(1)
                                .filter(e -> e.getPlayer().equals(getPlayer()))
                                .handler(e -> {
                                    e.setCancelled(true);
                                    mine.whitelist(Players.getOfflineNullable(e.getMessage()));
                                });
                    }));
        }
    }
}
