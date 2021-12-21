package me.pwo.evilprisoncore.credits.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class CreditsGiveCommand extends CreditsCommand {
    public CreditsGiveCommand(Credits credits) {
        super(credits);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2) {
            try {
                long amount = Long.parseLong(list.get(1));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.credits.getCreditsManager().giveCredits(player, amount);
                if (sender instanceof ConsoleCommandSender && player.isOnline())
                    PlayerUtils.sendMessage(player.getPlayer(), "&eYou have received &4☀&c%credits%&e."
                            .replace("%credits%", String.valueOf(amount))
                            .replace("%player%", sender.getName()), true);
                if (!(sender instanceof ConsoleCommandSender)) {
                    PlayerUtils.sendMessage(sender, "&aSuccessfully given &4☀&c%credits% &ato &6%player%&a."
                            .replace("%player%", player.getName())
                            .replace("%credits%", String.valueOf(amount)), true);
                }
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(CREDITS_ADMIN_PERMISSION);
    }
}
