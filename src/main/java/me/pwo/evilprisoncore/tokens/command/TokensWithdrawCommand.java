package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokensWithdrawCommand extends TokensCommand {
    public TokensWithdrawCommand(Tokens tokens) {
        super(tokens);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 1 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                long value = Long.parseLong(list.get(0));
                int amount = list.size() > 1 ? Integer.parseInt(list.get(1)) : 1;
                if (value <= 0L || amount <= 0) return false;
                if (this.tokens.getTokensManager().getPlayerTokens(player) < value * amount) {
                    PlayerUtils.sendMessage(player, "&c&l(!) Not Enough Tokens");
                    return false;
                }
                this.tokens.getTokensManager().withdrawTokens(player, value, amount);
                PlayerUtils.sendMessage(player, "&6&lTOKENS &8» &eYou have withdrawn &6%amount% x ⛁%value%&e!"
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%value%", String.valueOf(value)));
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