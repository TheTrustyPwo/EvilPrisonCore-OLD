package me.pwo.evilprisoncore.menu;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.menu.gui.PrisonMenuGUI;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Menu implements EvilPrisonModule {
    private static Menu instance;
    private EvilPrisonCore plugin;
    private Map<UUID, Boolean> menuToggled;
    private boolean enabled;

    public static Menu getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public Menu(EvilPrisonCore plugin) {
        this.plugin = plugin;
        menuToggled = new HashMap<>();
        registerEvents();
        registerCommands();
    }

    @Override
    public void enable() {
        instance = this;
        this.enabled = false;
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.menuToggled.put(e.getPlayer().getUniqueId(), true)).bindWith(this.plugin);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.menuToggled.remove(e.getPlayer().getUniqueId())).bindWith(this.plugin);
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR))
                .filter(e -> this.menuToggled.containsKey(e.getPlayer().getUniqueId()))
                .filter(e -> e.getItem().equals(getMenuItem()))
                .handler(e -> (new PrisonMenuGUI(e.getPlayer())).open()).bindWith(this.plugin);
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(context -> toggleMenu(context.sender())).registerAndBind(this.plugin, "togglemenu", "menutoggle");
        Commands.create()
                .assertPlayer()
                .handler(context -> (new PrisonMenuGUI(context.sender())).open()).registerAndBind(this.plugin, "menu", "prisonmenu");
    }

    public void toggleMenu(Player player) {
        this.menuToggled.replace(player.getUniqueId(),
                !this.menuToggled.get(player.getUniqueId()));
        if (this.menuToggled.get(player.getUniqueId())) {
            player.getInventory().setItem(8, getMenuItem());
            PlayerUtils.sendMessage(player, "&eYou have &c&lDISABLED &ethe Prison Menu.");
        } else {
            player.getInventory().remove(getMenuItem());
            PlayerUtils.sendMessage(player, "&eYou have &a&lENABLED &ethe Prison Menu.");
        }
    }

    private ItemStack getMenuItem() {
        ItemStack itemStack = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Text.colorize("&6Prison Menu &f&o(Right Click)"));
        itemMeta.setLore(Arrays.asList(Text.colorize("&f/togglemenu to hide this item.")));
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {

    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Menu";
    }
}
