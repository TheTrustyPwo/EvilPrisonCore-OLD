package me.pwo.evilprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class GangChatCommand extends GangsCommand {
    public GangChatCommand(EvilPrisonGangs evilPrisonGangs) {
        super(evilPrisonGangs);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (sender instanceof Player && list.size() == 0) {
            Player player = (Player) sender;
            Optional<Gang> gang = this.evilPrisonGangs.getGangManager().getPlayerGang(player);
            if (!gang.isPresent()) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cYou are not in a gang!");
                return false;
            }
            PlayerUtils.sendMessage(sender, "&eYou have %toggle% &egang chat!"
                    .replaceAll("%toggle%",
                            this.evilPrisonGangs.getGangManager().toggleGangChat(player) ? "&a&lENABLED" : "&c&lDISABLED"));
            return true;
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        return null;
    }
}
