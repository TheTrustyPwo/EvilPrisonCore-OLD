package me.pwo.evilprisoncore.events.gui;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class EventsGUI extends Gui {
    public EventsGUI(Player player) {
        super(player, 3, Text.colorize("&8Events"));
    }

    @Override
    public void redraw() {

    }
}
