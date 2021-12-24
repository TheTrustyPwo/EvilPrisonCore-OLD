package me.pwo.evilprisoncore.blocks.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class BlocksSetCommand extends BlocksCommand {
    public BlocksSetCommand(Blocks blocks) {
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
                this.blocks.getBlocksManager().setBlocks(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully set &6%player%'s &ablocks to &6&n%blocks%&a."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%blocks%", String.valueOf(amount)), true);
                return true;
            } catch (Exception exception) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(BLOCKS_ADMIN_PERMISSION);
    }
}
