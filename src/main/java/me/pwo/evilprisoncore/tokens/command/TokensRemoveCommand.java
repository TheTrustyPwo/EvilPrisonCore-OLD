package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TokensRemoveCommand extends TokensCommand {
    public TokensRemoveCommand(Tokens tokens) {
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
                this.tokens.getTokensManager().removeTokens(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully removed &6⛁%tokens% &afrom &6%player%."
                        .replace("%player%", player.getName())
                        .replace("%tokens%", String.valueOf(amount)));
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(TOKENS_ADMIN_PERMISSION);
    }
}
