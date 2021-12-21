package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokensPayCommand extends TokensCommand {
    public TokensPayCommand(Tokens tokens) {
        super(tokens);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                long amount = Long.parseLong(list.get(1).replaceAll(",", ""));
                if (amount <= 0L)
                    return false;
                OfflinePlayer receiver = Players.getOfflineNullable(list.get(0));
                if (receiver == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                if (receiver.getUniqueId().equals(player.getUniqueId())) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cYou can't send tokens to yourself");
                    return false;
                }
                this.tokens.getTokensManager().removeTokens(player, amount);
                this.tokens.getTokensManager().giveTokens(receiver, amount, false);
                PlayerUtils.sendMessage(player, "&eYou have sent &6⛁%tokens% &eto &6%player%&e."
                        .replaceAll("%player%", receiver.getName())
                        .replaceAll("%tokens%", String.valueOf(amount)), true);
                if (receiver.isOnline())
                    PlayerUtils.sendMessage(receiver.getPlayer(), "&eYou received &6⛁%tokens% &efrom &6%player%&e."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%tokens%", String.valueOf(amount)), true);
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return true;
    }
}
