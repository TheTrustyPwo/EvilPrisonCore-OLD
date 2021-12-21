package me.pwo.evilprisoncore.credits.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.gems.Gems;
import org.bukkit.command.CommandSender;

public abstract class CreditsCommand {
    protected Credits credits;
    public static final String CREDITS_ADMIN_PERMISSION = "evilprison.credits.admin";

    public CreditsCommand(Credits credits) {
        this.credits = credits;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);
}
