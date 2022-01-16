package me.pwo.evilprisoncore.gangs.gui;

import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.utils.gui.ConfirmationGUI;
import org.bukkit.entity.Player;

public class GangDisbandGUI extends ConfirmationGUI {
    private final EvilPrisonGangs evilPrisonGangs;

    public GangDisbandGUI(EvilPrisonGangs evilPrisonGangs, Player player) {
        super(player, "&8Confirm Gang Disband?");
        this.evilPrisonGangs = evilPrisonGangs;
    }

    @Override
    public void confirm() {
        Schedulers.async().run(() -> evilPrisonGangs.getGangManager().removeGang(getPlayer()));
        close();
    }
}
