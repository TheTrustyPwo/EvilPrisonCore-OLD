package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsPayCommand extends GemsCommand {
    public GemsPayCommand(Gems gems) {
        super(gems);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 2 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                long amount = Long.parseLong(list.get(1).replaceAll(",", ""));
                if (amount <= 0L)
                    return false;
                OfflinePlayer receiver = Players.getOfflineNullable(list.get(0));
                if (receiver == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                if (receiver.getUniqueId().equals(player.getUniqueId())) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cYou can't send gems to yourself");
                    return false;
                }
                this.gems.getGemsManager().removeGems(player, amount);
                this.gems.getGemsManager().giveGems(receiver, amount, false);
                PlayerUtils.sendMessage(player, "&eYou have sent &b♦%gems% &eto &6%player%&e."
                        .replaceAll("%player%", receiver.getName())
                        .replaceAll("%gems%", String.valueOf(amount)), true);
                if (receiver.isOnline())
                    PlayerUtils.sendMessage(receiver.getPlayer(), "&eYou received &b♦%gems% &efrom &6%player%&e."
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%gems%", String.valueOf(amount)), true);
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return true;
    }
}
