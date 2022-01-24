package me.pwo.evilprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GangInviteCommand extends GangsCommand {
    public GangInviteCommand(EvilPrisonGangs evilPrisonGangs) {
        super(evilPrisonGangs);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (sender instanceof Player && list.size() == 1) {
            Player player = (Player) sender;
            Player target = Players.getNullable(list.get(0));
            if (target == null || !target.isOnline()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                return false;
            }
            Optional<Gang> gang = this.evilPrisonGangs.getGangManager().getPlayerGang(player);
            if (!gang.isPresent()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou are not in a gang!");
                return false;
            }
            if (!gang.get().isOwner(player)) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cOnly the gang owner can do this!");
                return false;
            }
            if (gang.get().isFull()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cGang is full!");
                return false;
            }
            if (this.evilPrisonGangs.getGangManager().getPendingInvites().containsKey(target.getUniqueId())) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cPlayer already has a pending invite!");
                return false;
            }
            this.evilPrisonGangs.getGangManager().invitePlayer(target, gang.get());
            return true;
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
