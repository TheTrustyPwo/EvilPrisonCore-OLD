package me.pwo.evilprisoncore.gems.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;

public class GemsHelpCommand extends GemsCommand {
    public GemsHelpCommand(Gems gems) {
        super(gems);
    }

    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.isEmpty()) {
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            PlayerUtils.sendMessage(sender, "&e&lGEMS HELP MENU ");
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            PlayerUtils.sendMessage(sender, "&e/gems pay [player] [amount]");
            PlayerUtils.sendMessage(sender, "&e/gems withdraw [amount] [value]");
            PlayerUtils.sendMessage(sender, "&e/gems [player]");
            if (sender.hasPermission(GEMS_ADMIN_PERMISSION)) {
                PlayerUtils.sendMessage(sender, "&e/gems give [player] [amount]");
                PlayerUtils.sendMessage(sender, "&e/gems remove [player] [amount]");
                PlayerUtils.sendMessage(sender, "&e/gems set [player] [amount]");
            }
            PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
            return true;
        }
        return false;
    }

    public boolean canExecute(CommandSender paramCommandSender) {
        return true;
    }
}
