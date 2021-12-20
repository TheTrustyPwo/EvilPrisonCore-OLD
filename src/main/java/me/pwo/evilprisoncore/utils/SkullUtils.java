package me.pwo.evilprisoncore.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class SkullUtils {
    // Mines GUIs
    public static final ItemStack VIEW_PUBLIC_MINE_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ==");
    public static final ItemStack PLAYER_MANAGEMENT_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVlNTIyMzMxN2E4OTBhMzAzNTFmNmY3OGQwYWJmOGRkNzZjYmQwOGRmNmY5MTg4ODM5MzQ1NjRkMjhlNThlIn19fQ==");
    public static final ItemStack TELEPORT_TO_MINE_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGMxNzU0ODUxZTM2N2U4YmViYTJhNmQ4ZjdjMmZlZGU4N2FlNzkzYWM1NDZiMGYyOTlkNjczMjE1YjI5MyJ9fX0=");
    public static final ItemStack RESET_ITEM_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNiMjI4ZjcwYTM1ZDBhYTMyMzUwNDY3ZDllOGMwOWFhZTlhZTBhZTA4NzVmZGM4YzMxMWE4NzZiZTE5MDcxNyJ9fX0=");
    public static final ItemStack MANAGE_BLOCKS_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg0MGI4N2Q1MjI3MWQyYTc1NWRlZGM4Mjg3N2UwZWQzZGY2N2RjYzQyZWE0NzllYzE0NjE3NmIwMjc3OWE1In19fQ==");
    public static final ItemStack MANAGE_ACCESS_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM1NzQ0NGFkZTY0ZWM2Y2VhNjQ1ZWM1N2U3NzU4NjRkNjdjNWZhNjIyOTk3ODZlMDM3OTkzMTdlZTRhZCJ9fX0=");
    public static final ItemStack TOGGLE_PUBLIC_ACCESS_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjcyYzA1ZGQ3NjI4OGY0MzI4YTI0MzkxYmY0NzI1ZmQyMjYwNTkyZGIzY2Y5YjJiYzIwMzJkZDA1OTZjZjQ0MCJ9fX0=");
    public static final ItemStack SALES_TAX_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODgxNjYwNjI2MDc3OWIyM2VkMTVmODdjNTZjOTMyMjQwZGI3NDVmODZmNjgzZDFmNGRlYjgzYTRhMTI1ZmE3YiJ9fX0=");
    public static final ItemStack REMOVE_PLAYER_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ==");

    // Prison Menu GUIs
    public static final ItemStack SERVER_LINKS_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=");
    public static final ItemStack VOTE_BUTTON_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZjI1Y2ZmZjc1NGYyODdhOTgzOGQ4ZWZlMDM5OTgwNzNjMjJkZjdhOWQzMDI1YzQyNWUzZWQ3ZmY1MmMyMCJ9fX0=");
    public static final ItemStack SETTINGS_BUTTON_GUI_ITEM = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQ0OGUyNzUzMTM1MzJmNTRjNGJhMjE4OTQ4MDlhMjNkY2U1MmFmMDFkZGQxZTg5ZmM3Njg5NDgxZmFiNzM3ZSJ9fX0=");

    public static final ItemStack BACK_BUTTON = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE2MmQ0MmY0ZGJhMzU0ODhmNGY2NmQ2NzM2MzViZmM1NjE5YmRkNTEzZDAyYjRjYzc0ZjA1ZWM4ZTk1NiJ9fX0=");
    public static final ItemStack FORWARD_BUTTON = getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjgxMzYzM2JkNjAxNTJkOWRmNTRiM2Q5ZDU3M2E4YmMzNjU0OGI3MmRjMWEzMGZiNGNiOWVjMjU2ZDY4YWUifX19");

    public static void init() {}

    public static ItemStack getCustomTextureHead(String paramString) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM);
        SkullMeta skullMeta = (SkullMeta)itemStack.getItemMeta();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", paramString));
        try {
            Field field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(skullMeta, gameProfile);
        } catch (IllegalArgumentException|IllegalAccessException|NoSuchFieldException|SecurityException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        }
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    public static ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwningPlayer(player);
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }
}
