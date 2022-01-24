package me.pwo.evilprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GangLeaveCommand extends GangsCommand {
    public GangLeaveCommand(EvilPrisonGangs evilPrisonGangs) {
        super(evilPrisonGangs);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (sender instanceof Player && list.size() == 0) {
            Player player = (Player) sender;
            Optional<Gang> gang = this.evilPrisonGangs.getGangManager().getPlayerGang(player);
            if (!gang.isPresent()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou are not in a gang!");
                return false;
            }
            if (gang.get().isOwner(player)) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou cannot leave the gang if you are the owner");
                return false;
            }
            gang.get().removePlayer(player);
            PlayerUtils.sendMessage(sender, "&e&l(!) &eYou have left the gang!");
            gang.get().getOnlinePlayers().forEach(member -> PlayerUtils.sendMessage(member, "&6%player% &ehas left the gang!"
                    .replaceAll("%player%", player.getName())));
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
