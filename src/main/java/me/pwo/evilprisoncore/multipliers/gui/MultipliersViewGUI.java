package me.pwo.evilprisoncore.multipliers.gui;

import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MultipliersViewGUI extends Gui {
    private static final MenuScheme FILLER = new MenuScheme()
            .mask("111111111")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("110111011");
    private static final MenuScheme MULTI = new MenuScheme()
            .mask("000000000")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("000000000");
    private final MultiplierType multiplierType;
    private final int page;

    public MultipliersViewGUI(Player player, MultiplierType multiplierType, int page) {
        super(player, 6, "&8Multipliers");
        this.multiplierType = multiplierType;
        this.page = page;
    }

    @Override
    public void redraw() {
        List<Multiplier> multipliers = Multipliers.getInstance().getMultipliersManager().getPlayerMultipliers(getPlayer(), multiplierType);
        int totalPages = (int) Math.floor((float) multipliers.size() / 28) + 1;
        MenuPopulator filler = FILLER.newPopulator(this);
        while (filler.hasSpace())
            filler.accept(ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Prev Page / Back
        if (this.page == 1) {
            setItem(47, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                    .name("&6Go Back")
                    .build(() -> new MultipliersGUI(getPlayer()).open()));
        } else {
            setItem(47, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                    .name("&e<- (%previous%/%max%)"
                            .replaceAll("%previous%", String.valueOf(this.page - 1))
                            .replaceAll("%max%", String.valueOf(totalPages)))
                    .build(() -> (new MultipliersViewGUI(getPlayer(), multiplierType, this.page - 1)).open()));
        }
        // Next Page
        if (this.page == totalPages) {
            setItem(51, ItemStackBuilder.of(SkullUtils.FORWARD_BUTTON.clone())
                    .name("&7Last Page").buildItem().build());
        } else {
            setItem(51, ItemStackBuilder.of(SkullUtils.FORWARD_BUTTON.clone())
                    .name("&e(%next%/%max%) ->"
                            .replaceAll("%next%", String.valueOf(this.page + 1))
                            .replaceAll("%max%", String.valueOf(totalPages)))
                    .build(() -> (new MultipliersViewGUI(getPlayer(), multiplierType, this.page + 1)).open()));
        }
        // Refresh
        setItem(49, ItemStackBuilder.of(SkullUtils.REFRESH.clone())
                .name("&6&lRefresh").lore("&7Click to refresh").build(this::redraw));
        // setItem(10, ItemStackBuilder.of());
        MenuPopulator multi = MULTI.newPopulator(this);
        int i = (this.page - 1) * 28;
        while (multi.hasSpace() && (i + 1) <= multipliers.size()) {
            multi.accept(getMultiplierGuiItem(multipliers.get(i)));
            i++;
        }
    }

    private Item getMultiplierGuiItem(Multiplier multiplier) {
        String primary = "&f";
        String secondary = "&f";
        switch (multiplier.getMultiplierType()) {
            case MONEY: {
                primary = "&2&l";
                secondary = "&a&l";
                break;
            } case TOKENS: {
                primary = "&e&l";
                secondary = "&6&l";
                break;
            } case GEMS: {
                primary = "&d&l";
                secondary = "&5&l";
                break;
            } case EXP: {
                primary = "&b&l";
                secondary = "&9&l";
                break;
            }
        }
        return ItemStackBuilder.of(MultiplierSource.getIcon(multiplier.getMultiplierSource()))
                .name("%secondary%%source%"
                        .replaceAll("%secondary%", secondary)
                        .replaceAll("%source%", multiplier.getMultiplierSource().name()))
                .lore(
                        " ",
                        "%primary%MULTIPLIER: &fx%multi%"
                                .replaceAll("%primary%", primary)
                                .replaceAll("%multi%", Utils.formatNumber(multiplier.getMultiplier())),
                        "%primary%DURATION: %duration%"
                                .replaceAll("%primary%", primary)
                                .replaceAll("%duration%", multiplier.getTimeLeftString()),
                        " "
                ).buildItem().build();
    }
}
