package me.pwo.evilprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class GangsCommand {
    protected EvilPrisonGangs evilPrisonGangs;
    public static final String GANGS_ADMIN_PERMISSION = "evilprison.gangs.admin";
        
    public GangsCommand(EvilPrisonGangs evilPrisonGangs) {
        this.evilPrisonGangs = evilPrisonGangs;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);

    public abstract List<String> onTabComplete(CommandSender sender, ImmutableList<String> list);
}
