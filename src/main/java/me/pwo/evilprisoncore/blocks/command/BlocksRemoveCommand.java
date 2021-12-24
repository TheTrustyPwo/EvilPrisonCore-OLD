package me.pwo.evilprisoncore.blocks.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class BlocksRemoveCommand extends BlocksCommand {
    public BlocksRemoveCommand(Blocks blocks) {
        super(blocks);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2)
            try {
                long amount = Long.parseLong(list.get(1));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.blocks.getBlocksManager().removeBlocks(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully removed &6&n%blocks% blocks &afrom &6%player%&a."
                        .replace("%player%", player.getName())
                        .replace("%blocks%", String.valueOf(amount)), true);
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(BLOCKS_ADMIN_PERMISSION);
    }
}