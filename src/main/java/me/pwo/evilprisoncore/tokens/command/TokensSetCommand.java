package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TokensSetCommand extends TokensCommand {
    public TokensSetCommand(Tokens tokens) {
        super(tokens);
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
                this.tokens.getTokensManager().setTokens(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully set &6%player%'s &atokens to &6⛁%tokens%&a."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%tokens%", String.valueOf(amount)), true);
                return true;
            } catch (Exception exception) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(TOKENS_ADMIN_PERMISSION);
    }
}
