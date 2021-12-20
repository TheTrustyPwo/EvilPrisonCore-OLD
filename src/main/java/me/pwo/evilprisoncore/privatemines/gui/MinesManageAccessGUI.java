package me.pwo.evilprisoncore.privatemines.gui;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class MinesManageAccessGUI extends Gui {
    public MinesManageAccessGUI(Player player) {
        super(player, 3, Text.colorize("&8Access Management"));
    }

    @Override
    public void redraw() {

    }
}
