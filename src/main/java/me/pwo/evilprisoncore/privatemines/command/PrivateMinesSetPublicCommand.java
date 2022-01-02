package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PrivateMinesSetPublicCommand extends PrivateMinesCommand {
    public PrivateMinesSetPublicCommand(PrivateMines privateMines) {
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
            boolean isPublic = Boolean.parseBoolean(list.get(0));
            mine.setPublic(isPublic);
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        return Arrays.asList("true", "false");
    }
}
