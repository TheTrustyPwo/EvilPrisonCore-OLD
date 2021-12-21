package me.pwo.evilprisoncore.utils;

import me.lucko.helper.text3.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class PlayerUtils {
    public static void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player && !((Player)sender).isOnline()) return;
        if (StringUtils.isBlank(message)) return;
        sender.sendMessage(Text.colorize(message));
    }

    public static void sendMessage(CommandSender sender, String message, boolean prefix) {
        if (sender instanceof Player && !((Player)sender).isOnline()) return;
        if (StringUtils.isBlank(message)) return;
        sender.sendMessage(prefix ? Text.colorize("&6&lE&e&lKD &8» ") + Text.colorize(message) : Text.colorize(message));
    }

    public static void sendMessage(CommandSender sender, List<String> messages, boolean prefix) {
        if (sender instanceof Player && !((Player)sender).isOnline()) return;
        for (String str : messages) {
            if (StringUtils.isBlank(str)) return;
            if (prefix) sender.sendMessage(Text.colorize("&6&lE&e&lKD &8» ") + Text.colorize(str));
            else sender.sendMessage(Text.colorize(str));
        }
    }

    public static void sendTitle(CommandSender paramCommandSender, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (paramCommandSender instanceof Player && !((Player)paramCommandSender).isOnline())
            return;
        Player player;
        if (paramCommandSender instanceof Player) {
            player = (Player) paramCommandSender;
            player.sendTitle(Text.colorize(title), Text.colorize(subtitle), fadeIn, stay, fadeOut);
        }
    }

    public static void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }
}
