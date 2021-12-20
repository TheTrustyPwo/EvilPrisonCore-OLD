package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsWithdrawCommand extends GemsCommand {
    public GemsWithdrawCommand(Gems gems) {
        super(gems);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 1 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                long value = Long.parseLong(list.get(0));
                int amount = list.size() > 1 ? Integer.parseInt(list.get(1)) : 1;
                if (value <= 0L || amount <= 0) return false;
                if (this.gems.getGemsManager().getPlayerGems(player) < value * amount) {
                    PlayerUtils.sendMessage(player, "&c&l(!) Not Enough Gems");
                    return false;
                }
                this.gems.getGemsManager().withdrawGems(player, value, amount);
                PlayerUtils.sendMessage(player, "&aYou have withdrawn &e%value%x %amount% Gems!"
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%value%", String.valueOf(value)));
                return true;
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        } return false;
    }

    public boolean canExecute(CommandSender sender) {
        return true;
    }
}