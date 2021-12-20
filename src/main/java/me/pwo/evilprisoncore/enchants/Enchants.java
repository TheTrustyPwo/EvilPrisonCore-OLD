package me.pwo.evilprisoncore.enchants;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import me.pwo.evilprisoncore.enchants.gui.DisenchantGUI;
import me.pwo.evilprisoncore.enchants.gui.EnchantGUI;
import me.pwo.evilprisoncore.enchants.manager.EnchantsManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class Enchants implements EvilPrisonModules {
    private static Enchants instance;
    private final EvilPrisonCore plugin;
    private EnchantsManager enchantsManager;
    private FileConfiguration enchantsConfig;
    private HashMap<String, String> messages;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public static Enchants getInstance() {
        return instance;
    }

    public EnchantsManager getEnchantsManager() {
        return enchantsManager;
    }

    public FileConfiguration getConfig() {
        return enchantsConfig;
    }

    public Enchants(EvilPrisonCore plugin) {
        this.plugin = plugin;
        instance = this;
    }

    @Override
    public void enable() {
        this.enchantsConfig = this.plugin.getFileUtils().getConfig("enchants.yml").copyDefaults(true).save().get();
        this.enchantsManager = new EnchantsManager(this);
        this.enabled = true;
        loadMessages();
        registerEvents();
        registerCommands();
        EvilPrisonEnchantment.loadDefaultEnchantments();
    }

    @Override
    public void disable() {
        for (Player player : Players.all())
            player.closeInventory();
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("enchants.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> (e.getItem().getType() == Material.DIAMOND_PICKAXE))
                .filter(e -> (e.getAction() == Action.RIGHT_CLICK_AIR || (e.getAction() == Action.RIGHT_CLICK_BLOCK)))
                .handler(e -> {
                    e.setCancelled(true);
                    ItemStack itemStack = e.getItem();
                    int i = this.enchantsManager.getInventorySlot(e.getPlayer(), itemStack);
                    (new EnchantGUI(e.getPlayer(), itemStack, i)).open();
                }).bindWith(this.plugin);
        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(paramBlockBreakEvent -> (paramBlockBreakEvent.getPlayer().getGameMode() == GameMode.SURVIVAL && !paramBlockBreakEvent.isCancelled() && paramBlockBreakEvent.getPlayer().getItemInHand() != null && getPlugin().isPickaxeSupported(paramBlockBreakEvent.getPlayer().getItemInHand().getType())))
                .filter(paramBlockBreakEvent -> (this.enchantsManager.isAllowEnchantsOutside() || WorldGuardWrapper.getInstance().getRegions(paramBlockBreakEvent.getBlock().getLocation()).stream().anyMatch((region) -> region.getId().toLowerCase().startsWith("mine-"))))
                .handler(paramBlockBreakEvent -> {
                    this.enchantsManager.addBlocksBrokenToItem(paramBlockBreakEvent.getPlayer().getItemInHand(), 1);
                    this.enchantsManager.handleBlockBreak(paramBlockBreakEvent, paramBlockBreakEvent.getPlayer().getItemInHand());
                }).bindWith(this.plugin);
        Events.subscribe(BlockBreakEvent.class, EventPriority.LOWEST)
                .filter(paramBlockBreakEvent -> (paramBlockBreakEvent.getPlayer().getGameMode() == GameMode.SURVIVAL && !paramBlockBreakEvent.isCancelled() && paramBlockBreakEvent.getPlayer().getItemInHand() != null && getPlugin().isPickaxeSupported(paramBlockBreakEvent.getPlayer().getItemInHand().getType())))
                .filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().noneMatch((region) -> region.getId().toLowerCase().startsWith("mine-")))
                .filter(paramBlockBreakEvent -> this.enchantsManager.hasEnchants(paramBlockBreakEvent.getPlayer().getItemInHand()))
                .handler(paramBlockBreakEvent -> paramBlockBreakEvent.setCancelled(true)).bindWith(this.plugin);
        Events.subscribe(PlayerItemHeldEvent.class, EventPriority.HIGHEST)
                .handler(paramPlayerItemHeldEvent -> {
                    ItemStack itemStack1 = paramPlayerItemHeldEvent.getPlayer().getInventory().getItem(paramPlayerItemHeldEvent.getNewSlot());
                    ItemStack itemStack2 = paramPlayerItemHeldEvent.getPlayer().getInventory().getItem(paramPlayerItemHeldEvent.getPreviousSlot());
                    if (itemStack2 != null && getPlugin().isPickaxeSupported(itemStack2.getType()))
                        this.enchantsManager.handlePickaxeUnequip(paramPlayerItemHeldEvent.getPlayer(), itemStack2);
                    if (itemStack1 != null && getPlugin().isPickaxeSupported(itemStack1.getType()))
                        this.enchantsManager.handlePickaxeEquip(paramPlayerItemHeldEvent.getPlayer(), itemStack1);
                }).bindWith(this.plugin);
        Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST)
                .handler(paramPlayerDropItemEvent -> {
                    if (getPlugin().isPickaxeSupported(paramPlayerDropItemEvent.getItemDrop().getItemStack()))
                        this.enchantsManager.handlePickaxeUnequip(paramPlayerDropItemEvent.getPlayer(), paramPlayerDropItemEvent.getItemDrop().getItemStack());
                }).bindWith(this.plugin);
        Events.subscribe(PlayerJoinEvent.class)
                .filter(paramPlayerJoinEvent -> (!paramPlayerJoinEvent.getPlayer().hasPlayedBefore() && this.enchantsManager.isFirstJoinPickaxeEnabled()))
                .handler(paramPlayerJoinEvent -> {
                    ItemStack itemStack = this.enchantsManager.createFirstJoinPickaxe(paramPlayerJoinEvent.getPlayer());
                    paramPlayerJoinEvent.getPlayer().getInventory().addItem(itemStack);
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        Commands.create()
                .assertOp()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0) {
                        PlayerUtils.sendMessage(paramCommandContext.sender(), Text.colorize("&c/givepickaxe <player> <[enchant1]=[level1],[enchant2]=[level2],...[enchantX]=[levelX]> <pickaxe_name>"));
                        return;
                    }
                    String str1 = null;
                    String str2 = null;
                    Player player = null;
                    if (paramCommandContext.args().size() == 1) {
                        str1 = paramCommandContext.rawArg(0);
                    } else if (paramCommandContext.args().size() == 2) {
                        player = paramCommandContext.arg(0).parseOrFail(Player.class);
                        str1 = paramCommandContext.rawArg(1);
                    } else if (paramCommandContext.args().size() == 3) {
                        player = paramCommandContext.arg(0).parseOrFail(Player.class);
                        str1 = paramCommandContext.rawArg(1);
                        str2 = StringUtils.join(paramCommandContext.args().subList(2, paramCommandContext.args().size()), " ");
                    }
                    this.enchantsManager.givePickaxe(player, str1, str2, paramCommandContext.sender());
                }).registerAndBind(this.plugin, "givepickaxe");
        Commands.create()
                .assertOp()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0) {
                        PlayerUtils.sendMessage(paramCommandContext.sender(), Text.colorize("&c/givefirstjoinpickaxe <player>"));
                        return;
                    }
                    Player player = paramCommandContext.arg(0).parseOrFail(Player.class);
                    player.getInventory().addItem(this.enchantsManager.createFirstJoinPickaxe(player));
                    PlayerUtils.sendMessage(paramCommandContext.sender(), Text.colorize("&aYou have given first join pickaxe to &e" + player.getName()));
                }).registerAndBind(this.plugin, "givefirstjoinpickaxe");
        Commands.create()
                .assertPlayer()
                .handler(paramCommandContext -> {
                    ItemStack itemStack = paramCommandContext.sender().getItemInHand();
                    if (itemStack == null || !getPlugin().isPickaxeSupported(itemStack.getType())) {
                        PlayerUtils.sendMessage(paramCommandContext.sender(), getMessage("no_pickaxe_found"));
                        return;
                    }
                    int i = this.enchantsManager.getInventorySlot(paramCommandContext.sender(), itemStack);
                    (new DisenchantGUI(paramCommandContext.sender(), itemStack, i)).open();
                }).registerAndBind(this.plugin, "disenchant", "dise", "de", "disenchantmenu", "dismenu");
        Commands.create()
                .assertPlayer()
                .handler(paramCommandContext -> {
                    ItemStack itemStack = paramCommandContext.sender().getItemInHand();
                    if (itemStack == null || !getPlugin().isPickaxeSupported(itemStack.getType())) {
                        PlayerUtils.sendMessage(paramCommandContext.sender(), getMessage("no_pickaxe_found"));
                        return;
                    }
                    int i = this.enchantsManager.getInventorySlot(paramCommandContext.sender(), itemStack);
                    (new EnchantGUI(paramCommandContext.sender(), itemStack, i)).open();
                }).registerAndBind(this.plugin, "enchantmenu", "enchmenu");
    }

    private void loadMessages() {
        this.messages = new HashMap<>();
        for (String str : getConfig().getConfigurationSection("messages").getKeys(false))
            this.messages.put(str, Text.colorize(getConfig().getString("messages." + str)));
    }

    public String getMessage(String paramString) {
        return this.messages.getOrDefault(paramString.toLowerCase(), Text.colorize("&cMessage " + paramString + " not found."));
    }

    @Override
    public String getName() {
        return "Enchants";
    }
}
