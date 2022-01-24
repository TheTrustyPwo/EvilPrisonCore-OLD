package me.pwo.evilprisoncore.nms;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMSProvider_v1_8_R1 extends NMSProvider {

    @Override
    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.a((byte) 2));
        (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packetPlayOutChat);
    }
}
