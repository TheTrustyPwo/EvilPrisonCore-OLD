package me.pwo.evilprisoncore.tokens.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.tokens.Tokens;
import org.bukkit.command.CommandSender;

public abstract class TokensCommand {
    protected Tokens tokens;
    public static final String TOKENS_ADMIN_PERMISSION = "evilprison.tokens.admin";

    TokensCommand(Tokens tokens) {
        this.tokens = tokens;
    }

    public abstract boolean execute(CommandSender paramCommandSender, ImmutableList<String> paramImmutableList);

    public abstract boolean canExecute(CommandSender paramCommandSender);
}
