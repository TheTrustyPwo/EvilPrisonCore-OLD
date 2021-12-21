package me.pwo.evilprisoncore.pickaxe.pickaxeboosters.gui;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class PickaxeBoostersGUI extends Gui {
    public PickaxeBoostersGUI(Player player) {
        super(player, 1, Text.colorize("&8Pickaxe Boosters"));
    }

    @Override
    public void redraw() {

    }
}
