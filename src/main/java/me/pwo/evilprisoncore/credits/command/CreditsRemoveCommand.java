package me.pwo.evilprisoncore.credits.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class CreditsRemoveCommand extends CreditsCommand {
    public CreditsRemoveCommand(Credits credits) {
        super(credits);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2)
            try {
                long amount = Long.parseLong(list.get(1));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.credits.getCreditsManager().removeCredits(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully removed &4☀&c%credits% &afrom &6%player%&a."
                        .replace("%player%", player.getName())
                        .replace("%credits%", String.valueOf(amount)), true);
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(CREDITS_ADMIN_PERMISSION);
    }
}