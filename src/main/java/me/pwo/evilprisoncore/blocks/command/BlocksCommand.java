package me.pwo.evilprisoncore.blocks.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.gems.Gems;
import org.bukkit.command.CommandSender;

public abstract class BlocksCommand {
    protected Blocks blocks;
    public static final String BLOCKS_ADMIN_PERMISSION = "evilprison.blocks.admin";

    BlocksCommand(Blocks blocks) {
        this.blocks = blocks;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> list);

    public abstract boolean canExecute(CommandSender sender);
}
