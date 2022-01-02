package me.pwo.evilprisoncore.privatemines.command;

import com.google.common.collect.ImmutableList;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PrivateMinesSetTaxCommand extends PrivateMinesCommand {
    public PrivateMinesSetTaxCommand(PrivateMines privateMines) {
        super(privateMines);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) {
            try {
                Player player = (Player) sender;
                Mine mine = this.privateMines.getPrivateMinesManager().getPlayerMine(player.getUniqueId());
                if (mine == null) {
                    PlayerUtils.sendMessage(player, "&c&l(!) &cYou do not have a private mine! Create one with /mines create");
                    return false;
                }
                double tax = Double.parseDouble(list.get(0));
                if (tax < 0.0D || tax > 10.0D) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cSales Tax must be >= 0.0% and <= 10.0%");
                    return false;
                }
                mine.setTax(tax);
                PlayerUtils.sendMessage(sender, "&aYou updated your mine's Sales Tax value to %value%%"
                        .replaceAll("%value%", String.valueOf(tax)));
            } catch (NumberFormatException e) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) return Arrays.asList("1.0", "5.0", "10.0");
        return null;
    }
}
