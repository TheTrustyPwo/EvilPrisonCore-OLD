package me.pwo.evilprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.Gangs;
import org.bukkit.command.CommandSender;

public abstract class GangsCommand {
    protected Gangs gangs;
    public static final String GANGS_ADMIN_PERMISSION = "evilprison.gangs.admin";
        
    public GangsCommand(Gangs gangs) {
        this.gangs = gangs;
    }

    public abstract boolean execute(CommandSender paramCommandSender, ImmutableList<String> paramImmutableList);

    public abstract boolean canExecute(CommandSender paramCommandSender);
}
