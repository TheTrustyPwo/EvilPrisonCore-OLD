package me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.PickaxeRenameToken;
import me.pwo.evilprisoncore.tokens.Tokens;
import org.bukkit.command.CommandSender;

public abstract class PickaxeRenameTokenCommand {
    protected PickaxeRenameToken pickaxeRenameToken;
    public static final String PICKAXE_RENAME_TOKEN_ADMIN_PERMISSION = "evilprison.pickaxe.renametoken.admin";

    PickaxeRenameTokenCommand(PickaxeRenameToken pickaxeRenameToken) {
        this.pickaxeRenameToken = pickaxeRenameToken;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);
}
