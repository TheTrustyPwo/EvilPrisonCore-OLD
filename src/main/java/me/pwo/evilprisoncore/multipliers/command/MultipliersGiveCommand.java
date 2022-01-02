package me.pwo.evilprisoncore.multipliers.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultipliersGiveCommand extends MultipliersCommand {
    public MultipliersGiveCommand(Multipliers multipliers) {
        super(multipliers);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 4 || list.size() == 5) {
            try {
                Player target = Players.getNullable(list.get(0));
                if (target == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                double amount = Utils.round(Double.parseDouble(list.get(1)), 2);
                long time = Long.parseLong(list.get(2));
                MultiplierType type = MultiplierType.valueOf(list.get(3));
                MultiplierSource source = list.size() == 5 ? MultiplierSource.valueOf(list.get(4)) : MultiplierSource.MISCELLANEOUS;
                this.multipliers.getMultipliersManager().givePlayerMultiplier(target, amount, time, type, source);
                PlayerUtils.sendMessage(sender, "done");
            } catch (NumberFormatException numberFormatException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            } catch (IllegalArgumentException illegalArgumentException) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cNo such multiplier type or source!");
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(MULTIPLIERS_ADMIN_PERMISSION);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, ImmutableList<String> list) {
        if (list.size() == 1) return null;
        else if (list.size() == 2) return Arrays.asList("1.0", "2.0", "3.0");
        else if (list.size() == 3) return Arrays.asList("60", "1800", "3600", "7200");
        else if (list.size() == 4) return Stream.of(MultiplierType.values()).map(MultiplierType::name).collect(Collectors.toList());
        else if (list.size() == 5) return Stream.of(MultiplierSource.values()).map(MultiplierSource::name).collect(Collectors.toList());
        return null;
    }
}
