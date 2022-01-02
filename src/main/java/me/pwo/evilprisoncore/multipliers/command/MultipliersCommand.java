package me.pwo.evilprisoncore.multipliers.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class MultipliersCommand {
    protected Multipliers multipliers;
    public static final String MULTIPLIERS_ADMIN_PERMISSION = "evilprison.multipliers.admin";

    public MultipliersCommand(Multipliers multipliers) {
        this.multipliers = multipliers;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);

    public abstract List<String> onTabComplete(CommandSender sender, ImmutableList<String> list);
}
