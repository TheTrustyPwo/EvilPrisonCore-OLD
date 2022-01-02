package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class PrivateMinesCommand {
    protected PrivateMines privateMines;
    public static final String PRIVATE_MINES_ADMIN_PERMISSION = "evilprison.privatemines.admin";

    public PrivateMinesCommand(PrivateMines privateMines) {
        this.privateMines = privateMines;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);

    public abstract List<String> onTabComplete(CommandSender sender, ImmutableList<String> list);
}
