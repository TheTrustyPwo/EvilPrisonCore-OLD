package me.pwo.evilprisoncore.menu.gui;

import com.sk89q.worldedit.util.command.parametric.StringArgumentStack;
import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerProfileGUI extends Gui {
    private OfflinePlayer target;

    public PlayerProfileGUI(Player player, OfflinePlayer target) {
        super(player, 6, "&8%player%'s Profile".replaceAll("%player%", target.getName()));
        this.target = target;
    }

    @Override
    public void redraw() {
        // Player
        setItem(4, ItemStackBuilder.of(SkullCreator.itemFromUuid(target.getUniqueId()))
                .name("&6%player% %online%"
                        .replaceAll("%player%", target.getName())
                        .replaceAll("%online%", target.isOnline() ? "&a&lONLINE" : "&c&lOFFLINE"))
                .lore(
                        "Rank",
                        "&6%tokens% Tokens".replaceAll("%tokens%", Utils.formatNumber(Tokens.getInstance().getApi().getPlayerTokens(target))),
                        "&6%gems% Gems".replaceAll("%gems%", Utils.formatNumber(Gems.getInstance().getApi().getPlayerGems(target)))
                ).buildItem().build());
    }
}
