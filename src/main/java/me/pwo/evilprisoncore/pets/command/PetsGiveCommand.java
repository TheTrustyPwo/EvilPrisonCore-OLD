package me.pwo.evilprisoncore.pets.command;

import com.google.common.collect.ImmutableList;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pets.pets.EvilPet;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetsGiveCommand extends PetsCommand {

    public PetsGiveCommand(Pets pets) {
        super(pets);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> list) {
        if (list.size() >= 2) {
            try {
                Player player = Players.getNullable(list.get(0));
                if (player == null) {
                    PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Player");
                    return false;
                }
                EvilPet pet = EvilPet.getPetById(Integer.parseInt(list.get(1)));
                int tier = list.size() >= 3 ? Integer.parseInt(list.get(2)) : 1;
                int level = list.size() >= 4 ? Integer.parseInt(list.get(3)) : 1;
                long exp = list.size() >= 5 ? Long.parseLong(list.get(4)) : 0;
                Pets.getInstance().getPetsManager().givePet(player, pet, exp, level, tier);
                PlayerUtils.sendMessage(sender, "&aSuccessfully given %pet% &e(Tier: &6%tier%&e, Level: &6%level%&e, Exp: &6%exp%&e)&ato &6%player%&a."
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%pet%", pet.getName())
                        .replaceAll("%level%", String.valueOf(level))
                        .replaceAll("%tier%", String.valueOf(tier))
                        .replaceAll("%exp%", String.valueOf(exp)), true);
            } catch (NumberFormatException e) {
                PlayerUtils.sendMessage(sender, "&c&l(!) &cInvalid Number");
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(PETS_ADMIN_PERMISSION);
    }
}
