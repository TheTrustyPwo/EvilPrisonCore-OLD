package me.pwo.evilprisoncore.menu.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class PlayerVaultsGUI extends Gui {
    private static final int maxVaults = 14;
    private static final MenuScheme VAULTS = new MenuScheme()
            .mask("000000000")
            .mask("011111110")
            .mask("011111110")
            .mask("000000000");

    public PlayerVaultsGUI(Player player) {
        super(player, 4, Text.colorize("&8Player Vaults"));
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Back Button
        setItem(0, ItemStackBuilder.of(SkullUtils.BACK_BUTTON)
                .name(Text.colorize("&6Go Back"))
                .buildItem().bind(e -> {
                    close();
                    (new PrisonMenuGUI(getPlayer())).open();
                }, ClickType.LEFT).build());
        // Vaults
        int numberOfVaults = getPlayerVaults(getPlayer());
        MenuPopulator menuPopulator = VAULTS.newPopulator(this);
        for (int vault = 1; vault<= maxVaults; vault++) {
            if (vault <= numberOfVaults) {
                int finalVault = vault;
                menuPopulator.accept(ItemStackBuilder.of(Material.ENDER_CHEST)
                        .name(Text.colorize("&aVault #%number%".replaceAll("%number%", String.valueOf(vault))))
                        .lore(Arrays.asList(
                                Text.colorize("&a&l| &fYou have access to this vault"),
                                " ",
                                Text.colorize("&a&lLEFT-CLICK &ato open this vault")
                        )).buildItem().bind(e -> {
                            close();
                            getPlayer().performCommand("pv %number%".replaceAll("%number%", String.valueOf(finalVault)));
                        }, ClickType.LEFT).build());
            } else {
                menuPopulator.accept(ItemStackBuilder.of(Material.CHEST)
                        .name(Text.colorize("&cVault #%number%".replaceAll("%number%", String.valueOf(vault))))
                        .lore(Arrays.asList(
                                Text.colorize("&c&l| &fYou don't have access to this vault"),
                                Text.colorize("&c&l| &fYou can access more vaults by purchasing a rank"),
                                Text.colorize("&c&l| &fPurchase ranks at store.evilkingdom.net")
                        )).buildItem().build());
            }
        }
    }

    private int getPlayerVaults(Player player) {
        for (int i = 1; i <= maxVaults; i++) {
            if (player.hasPermission("playervaults.amount." + i)) {
                return i;
            }
        }
        return 0;
    }
}
