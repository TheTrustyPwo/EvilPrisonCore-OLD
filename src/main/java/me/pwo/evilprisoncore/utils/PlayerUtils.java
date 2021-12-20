package me.pwo.evilprisoncore.utils;

import me.lucko.helper.text3.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {
    public static void sendMessage(CommandSender paramCommandSender, String paramString) {
        if (paramCommandSender instanceof Player && !((Player)paramCommandSender).isOnline())
            return;
        if (StringUtils.isBlank(paramString))
            return;
        paramCommandSender.sendMessage(Text.colorize(paramString).split("%nl%"));
    }

    public static void sendMessage(CommandSender paramCommandSender, List<String> paramList) {
        if (paramCommandSender instanceof Player && !((Player)paramCommandSender).isOnline())
            return;
        for (String str : paramList) {
            if (StringUtils.isBlank(str))
                return;
            paramCommandSender.sendMessage(Text.colorize(str));
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
