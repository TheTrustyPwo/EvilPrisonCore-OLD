package me.pwo.evilprisoncore.credits.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreditsWithdrawCommand extends CreditsCommand {
    public CreditsWithdrawCommand(Credits credits) {
        super(credits);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                long value = Long.parseLong(list.get(0));
                if (value < Credits.getInstance().getConfig().getLong("Minimum-Credits-Withdrawal")) return false;
                if (this.credits.getCreditsManager().getPlayerCredits(player) < value) {
                    PlayerUtils.sendMessage(player, "&c&l(!) &cNot Enough Credits");
                    return false;
                }
                this.credits.getCreditsManager().withdrawCredits(player, value);
                PlayerUtils.sendMessage(player, "&eYou have withdrawn &4☀&c%credits%&e!"
                        .replace("%credits%", String.valueOf(value)), true);
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        } return false;
    }

    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
