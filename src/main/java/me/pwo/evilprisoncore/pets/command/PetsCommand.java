package me.pwo.evilprisoncore.pets.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.pets.Pets;
import org.bukkit.command.CommandSender;

public abstract class PetsCommand {
    protected Pets pets;
    public static final String PETS_ADMIN_PERMISSION = "evilprison.pets.admin";

    public PetsCommand(Pets pets) {
        this.pets = pets;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);
}
