package me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.PickaxeRenameToken;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PickaxeRenameTokenGiveCommand extends PickaxeRenameTokenCommand {

    public PickaxeRenameTokenGiveCommand(PickaxeRenameToken pickaxeRenameToken) {
        super(pickaxeRenameToken);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2) {
            try {
                int amount = Integer.parseInt(list.get(1));
                Player player = Players.getNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                player.getInventory().addItem(PickaxeRenameToken.getInstance().createRenameTokenItem(amount));
                if (sender instanceof ConsoleCommandSender && player.isOnline())
                    PlayerUtils.sendMessage(player.getPlayer(), "&e&lPICKAXE &8&7You have received &f%amount% Rename Tokens&7."
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%player%", sender.getName()));
                if (!(sender instanceof ConsoleCommandSender)) {
                    PlayerUtils.sendMessage(sender, "&aSuccessfully given &e%amount% &aRename Tokens to &e%player%."
                            .replace("%player%", player.getName())
                            .replace("%amount%", String.valueOf(amount)));
                }
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(PICKAXE_RENAME_TOKEN_ADMIN_PERMISSION);
    }
}
