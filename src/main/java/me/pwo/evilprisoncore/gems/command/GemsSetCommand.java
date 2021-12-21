package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class GemsSetCommand extends GemsCommand {
    public GemsSetCommand(Gems gems) {
        super(gems);
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
                this.gems.getGemsManager().setGems(player, amount);
                PlayerUtils.sendMessage(sender, "&aSuccessfully set &6%player%'s &agems to &b♦%gems%&a."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%gems%", String.valueOf(amount)), true);
                return true;
            } catch (Exception exception) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return paramCommandSender.hasPermission(GEMS_ADMIN_PERMISSION);
    }
}
