package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PrivateMinesSetSizeCommand extends PrivateMinesCommand {
    public PrivateMinesSetSizeCommand(PrivateMines privateMines) {
        super(privateMines);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        try {
            if (list.size() == 2) {
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                int size = Integer.parseInt(list.get(1));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                Mine mine = this.privateMines.getPrivateMinesManager().getPlayerMine(player.getUniqueId());
                if (mine == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &c%player% does not have a private mine!"
                            .replaceAll("%player%", player.getName()));
                    return false;
                }
                mine.setMineSize(size);
                PlayerUtils.sendMessage(sender, "&aSuccessfully set &6%player%'s &amine size to &6%size%&a."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%size%", String.valueOf(size)));
                return true;
            }
        } catch (NumberFormatException numberFormatException) {
            PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(PRIVATE_MINES_ADMIN_PERMISSION);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) return null;
        else if (list.size() == 2) return Arrays.asList("11", "149");
        return null;
    }
}
