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

public class PrivateMinesCreateCommand extends PrivateMinesCommand {
    public PrivateMinesCreateCommand(PrivateMines privateMines) {
        super(privateMines);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        Player player = (Player) sender;
        if (this.privateMines.getPrivateMinesManager().getPlayerMine(player.getUniqueId()) != null) {
            PlayerUtils.sendMessage(player, "&c&l(!) &cYou already own a private mine!");
            return false;
        }
        this.privateMines.getPrivateMinesManager().createMine(player);
        PlayerUtils.sendMessage(player, "&aSuccessfully created a private mine!");
        return true;
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
