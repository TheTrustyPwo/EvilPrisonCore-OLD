package me.pwo.evilprisoncore.multipliers.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MultipliersGUI extends Gui {

    public MultipliersGUI(Player player) {
        super(player, 3, "&8Multipliers");
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Money Multiplier
        setItem(10, ItemStackBuilder.of(SkullUtils.MONEY_MULTI_GUI_ITEM.clone())
                .name("&a&lMoney &2&lMultipliers")
                .lore(
                        " ",
                        "&2&lTOTAL: &fx%total% &2(&a%amount%&2)"
                                .replaceAll("%total%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getTotalPlayerMultiplier(getPlayer(), MultiplierType.MONEY)))
                                .replaceAll("%amount%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getPlayerMultiplierAmount(getPlayer(), MultiplierType.MONEY))),
                        " ",
                        "&7Click to view all the &fMoney Multipliers",
                        "&7you have equipped on the server!"
                ).build(() -> (new MultipliersViewGUI(getPlayer(), MultiplierType.MONEY, 1)).open()));
        // Token Multiplier
        setItem(12, ItemStackBuilder.of(SkullUtils.TOKEN_MULTI_GUI_ITEM.clone())
                .name("&6&lToken &e&lMultipliers")
                .lore(
                        " ",
                        "&e&lTOTAL: &fx%total% &e(&6%amount%&e)"
                                .replaceAll("%total%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getTotalPlayerMultiplier(getPlayer(), MultiplierType.TOKENS)))
                                .replaceAll("%amount%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getPlayerMultiplierAmount(getPlayer(), MultiplierType.TOKENS))),
                        " ",
                        "&7Click to view all the &fToken Multipliers",
                        "&7you have equipped on the server!"
                ).build(() -> (new MultipliersViewGUI(getPlayer(), MultiplierType.TOKENS, 1)).open()));
        // Gems Multiplier
        setItem(14, ItemStackBuilder.of(SkullUtils.GEMS_MULTI_GUI_ITEM.clone())
                .name("&5&lGem &d&lMultipliers")
                .lore(
                        " ",
                        "&d&lTOTAL: &fx%total% &d(&5%amount%&d)"
                                .replaceAll("%total%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getTotalPlayerMultiplier(getPlayer(), MultiplierType.GEMS)))
                                .replaceAll("%amount%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getPlayerMultiplierAmount(getPlayer(), MultiplierType.GEMS))),
                        " ",
                        "&7Click to view all the &fGem Multipliers",
                        "&7you have equipped on the server!"
                ).build(() -> (new MultipliersViewGUI(getPlayer(), MultiplierType.GEMS, 1)).open()));
        // Exp Multiplier
        setItem(16, ItemStackBuilder.of(SkullUtils.EXP_MULTI_GUI_ITEM.clone())
                .name("&9&lExp &b&lMultipliers")
                .lore(
                        " ",
                        "&b&lTOTAL: &fx%total% &b(&9%amount%&b)"
                                .replaceAll("%total%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getTotalPlayerMultiplier(getPlayer(), MultiplierType.EXP)))
                                .replaceAll("%amount%", Utils.formatNumber(Multipliers.getInstance().getMultipliersManager().getPlayerMultiplierAmount(getPlayer(), MultiplierType.EXP))),
                        " ",
                        "&7Click to view all the &fExp Multipliers",
                        "&7you have equipped on the server!"
                ).build(() -> (new MultipliersViewGUI(getPlayer(), MultiplierType.EXP, 1)).open()));
    }
}
