package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PrivateMinesUnwhitelistCommand extends PrivateMinesCommand {
    public PrivateMinesUnwhitelistCommand(PrivateMines privateMines) {
        super(privateMines);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) {
            Player player = (Player) sender;
            Mine mine = this.privateMines.getPrivateMinesManager().getPlayerMine(player.getUniqueId());
            if (mine == null) {
                PlayerUtils.sendMessage(player, "&c&l(!) &cYou do not have a private mine! Create one with /mines create");
                return false;
            }
            OfflinePlayer target = Players.getOfflineNullable(list.get(0));
            if (target == null) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                return false;
            }
            mine.unwhitelist(target);
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
