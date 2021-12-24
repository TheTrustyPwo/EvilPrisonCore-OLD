package me.pwo.evilprisoncore.blocks.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class BlocksGiveCommand extends BlocksCommand {
    public BlocksGiveCommand(Blocks blocks) {
        super(blocks);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 2) {
            try {
                long amount = Long.parseLong(list.get(1));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.blocks.getBlocksManager().giveBlocks(player, amount);
                if (!(sender instanceof ConsoleCommandSender)) {
                    PlayerUtils.sendMessage(sender, "&aSuccessfully given &6&n%blocks% blocks &ato &6%player%&a."
                            .replace("%player%", player.getName())
                            .replace("%blocks%", String.valueOf(amount)), true);
                }
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(BLOCKS_ADMIN_PERMISSION);
    }
}