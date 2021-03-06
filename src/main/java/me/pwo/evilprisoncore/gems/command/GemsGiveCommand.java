package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class GemsGiveCommand extends GemsCommand {
    public GemsGiveCommand(Gems gems) {
        super(gems);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 2) {
            try {
                long amount = Long.parseLong(list.get(1));
                boolean applyMultiplier = list.size() > 2 && Boolean.parseBoolean(list.get(2));
                OfflinePlayer player = Players.getOfflineNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                this.gems.getGemsManager().giveGems(player, amount,applyMultiplier);
                if (sender instanceof ConsoleCommandSender && player.isOnline())
                    PlayerUtils.sendMessage(player.getPlayer(), "&eYou have received &b♦%gems%&e."
                            .replace("%gems%", String.valueOf(amount))
                            .replace("%player%", sender.getName()), true);
                if (!(sender instanceof ConsoleCommandSender)) {
                    PlayerUtils.sendMessage(sender, "&aSuccessfully given &b♦%gems% &ato &6%player%&a."
                            .replace("%player%", player.getName())
                            .replace("%gems%", String.valueOf(amount)), true);
                }
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(GEMS_ADMIN_PERMISSION);
    }
}