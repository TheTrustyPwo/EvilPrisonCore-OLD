package me.pwo.evilprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GangCreateCommand extends GangsCommand {
    public GangCreateCommand(EvilPrisonGangs evilPrisonGangs) {
        super(evilPrisonGangs);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (sender instanceof Player && list.size() == 1) {
            Player player = (Player) sender;
            if (this.evilPrisonGangs.getGangManager().getPlayerGang(player).isPresent()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou are already in a gang!");
                return false;
            }
            this.evilPrisonGangs.getGangManager().createGang(list.get(0), player);
            PlayerUtils.sendMessage(sender, "&eYou have created gang &6%gang%&e!"
                    .replaceAll("%gang%", list.get(0)), true);
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        return null;
    }
}
