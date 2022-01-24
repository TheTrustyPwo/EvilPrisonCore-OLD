package me.pwo.evilprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GangAcceptCommand extends GangsCommand {
    public GangAcceptCommand(EvilPrisonGangs evilPrisonGangs) {
        super(evilPrisonGangs);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (sender instanceof Player && list.size() == 0) {
            Player player = (Player) sender;
            if (this.evilPrisonGangs.getGangManager().getPlayerGang(player).isPresent()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou are already in a gang!");
                return false;
            }
            if (!this.evilPrisonGangs.getGangManager().getPendingInvites().containsKey(player.getUniqueId())) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cNo pending invites!");
                return false;
            }
            if (this.evilPrisonGangs.getGangManager().getPendingInvites().get(player.getUniqueId()).isFull()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cGang is full!");
                return false;
            }
            Gang gang = this.evilPrisonGangs.getGangManager().getPendingInvites().get(player.getUniqueId());
            this.evilPrisonGangs.getGangManager().acceptPlayer(player);
            PlayerUtils.sendMessage(sender, "&eYou joined &6%gang%&e!"
                    .replaceAll("%gang%", gang.getGangName()));
            gang.broadcastToMembers("&6%player% &ehas joined the gang!"
                    .replaceAll("%player%", player.getName()));
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
