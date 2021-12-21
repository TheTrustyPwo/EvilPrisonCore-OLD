package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class TokensGiveCommand extends TokensCommand {
    public TokensGiveCommand(Tokens tokens) {
        super(tokens);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 2) {
            try {
                long amount = Long.parseLong(list.get(1));
                boolean applyMultiplier = list.size() > 2 && Boolean.parseBoolean(list.get(2));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.tokens.getTokensManager().giveTokens(player, amount,applyMultiplier);
                if (sender instanceof ConsoleCommandSender && player.isOnline())
                    PlayerUtils.sendMessage(player.getPlayer(), "&6&lTOKENS &8» &eYou have received &6⛁%tokens%&e."
                            .replace("%tokens%", String.valueOf(amount))
                            .replace("%player%", sender.getName()));
                if (!(sender instanceof ConsoleCommandSender)) {
                    PlayerUtils.sendMessage(sender, "&aSuccessfully given &6⛁%tokens% &ato &6%player%."
                            .replace("%player%", player.getName())
                            .replace("%tokens%", String.valueOf(amount)));
                }
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(TOKENS_ADMIN_PERMISSION);
    }
}
