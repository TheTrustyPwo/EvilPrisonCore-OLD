package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PrivateMinesForceSaveCommand extends PrivateMinesCommand {
    public PrivateMinesForceSaveCommand(PrivateMines privateMines) {
        super(privateMines);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) {
            OfflinePlayer player = Players.getOfflineNullable(list.get(0));
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
            this.privateMines.getPrivateMinesManager().savePlayerMine(player);
            PlayerUtils.sendMessage(sender, "&aSuccessfully force saved &6%player%'s &amine!"
                    .replaceAll("%player%", player.getName()));
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(PRIVATE_MINES_ADMIN_PERMISSION);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        return null;
    }
}
