package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gems.Gems;
import org.bukkit.command.CommandSender;

public abstract class GemsCommand {
    protected Gems gems;
    public static final String GEMS_ADMIN_PERMISSION = "evilprison.gems.admin";

    GemsCommand(Gems gems) {
        this.gems = gems;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);
}
