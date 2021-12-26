package me.pwo.evilprisoncore.pets.gui;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class PetsGUI extends Gui {
    public PetsGUI(Player player) {
        super(player, 6, Text.colorize("&8Pets"));
    }

    @Override
    public void redraw() {

    }
}
