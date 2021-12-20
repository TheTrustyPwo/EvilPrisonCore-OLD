package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {
    public TokensHelpCommand(Tokens tokens) {
        super(tokens);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.isEmpty()) {
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            PlayerUtils.sendMessage(sender, "&e&lTOKEN HELP MENU ");
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            PlayerUtils.sendMessage(sender, "&e/tokens pay [player] [amount]");
            PlayerUtils.sendMessage(sender, "&e/tokens withdraw [amount] [value]");
            PlayerUtils.sendMessage(sender, "&e/tokens [player]");
            if (sender.hasPermission(TOKENS_ADMIN_PERMISSION)) {
                PlayerUtils.sendMessage(sender, "&e/tokens give [player] [amount]");
                PlayerUtils.sendMessage(sender, "&e/tokens remove [player] [amount]");
                PlayerUtils.sendMessage(sender, "&e/tokens set [player] [amount]");
            }
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            return true;
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return true;
    }
}
