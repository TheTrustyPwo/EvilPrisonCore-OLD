package me.pwo.evilprisoncore.credits;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.credits.api.CreditsAPI;
import me.pwo.evilprisoncore.credits.api.CreditsAPIImpl;
import me.pwo.evilprisoncore.credits.command.*;
import me.pwo.evilprisoncore.credits.gui.CreditsGUI;
import me.pwo.evilprisoncore.credits.manager.CreditsManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;
import java.util.regex.Pattern;

public class Credits implements EvilPrisonModule {
    private static Credits instance;
    private final EvilPrisonCore plugin;
    private CreditsManager creditsManager;
    private FileConfiguration creditsConfig;
    private CreditsAPI creditsAPI;
    private Map<String, CreditsCommand> commands;
    private List<UUID> bypassGiftCardChat = new ArrayList<>();
    private boolean enabled;

    public static Credits getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public CreditsManager getCreditsManager() {
        return creditsManager;
    }

    public FileConfiguration getConfig() {
        return creditsConfig;
    }

    public CreditsAPI getApi() {
        return creditsAPI;
    }

    public Credits(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.creditsConfig = this.plugin.getFileUtils().getConfig("credits.yml").copyDefaults(true).save().get();
        this.creditsManager = new CreditsManager(this);
        this.creditsAPI = new CreditsAPIImpl(this.creditsManager);
        registerEvents();
        registerCommands();
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.creditsManager.stopUpdating();
        this.creditsManager.savePlayerDataOnDisable();
        this.enabled = false;
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getPlayer().getItemInHand().getType() == Material.PAPER)
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
                .filter(e -> e.getHand() != EquipmentSlot.OFF_HAND)
                .handler(e -> this.creditsManager.viewCode(e.getPlayer())).bindWith(this.plugin);
        Events.subscribe(AsyncPlayerChatEvent.class)
                .filter(e -> !bypassGiftCardChat.contains(e.getPlayer().getUniqueId()))
                .filter(e -> Pattern.compile("^\\d{4} \\d{4} \\d{4} \\d{4}$").matcher(e.getMessage()).find())
                .handler(e -> {
                    e.setCancelled(true);
                    PlayerUtils.sendMessage(e.getPlayer(), "&c&lOH NO! &cDid you accidentally send a gift card code? Type '&4continue&c' if you want to send it.");
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer().equals(e.getPlayer()))
                            .handler(event -> {
                                e.setCancelled(true);
                                if (event.getMessage().equalsIgnoreCase("continue")) {
                                    this.bypassGiftCardChat.add(event.getPlayer().getUniqueId());
                                    event.getPlayer().chat(e.getMessage());
                                    this.bypassGiftCardChat.remove(event.getPlayer().getUniqueId());
                                }
                            }).bindWith((TerminableConsumer) this);
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new CreditsGiveCommand(this));
        this.commands.put("remove", new CreditsRemoveCommand(this));
        this.commands.put("set", new CreditsSetCommand(this));
        this.commands.put("withdraw", new CreditsWithdrawCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        (new CreditsGUI((Player) context.sender())).open();
                        return;
                    }
                    CreditsCommand creditsCommand = this.commands.get(context.rawArg(0));
                    if (creditsCommand != null) {
                        if (creditsCommand.canExecute(context.sender())) {
                            creditsCommand.execute(context.sender(), context.args().subList(1, context.args().size()));
                        } else {
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                        }
                    } else {
                        (new CreditsGUI((Player) context.sender())).open();
                    }
                }).registerAndBind(this.plugin, "credits", "credit");
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0)
                        this.creditsManager.sendCreditsTop(context.sender());
                }).registerAndBind(this.plugin, "creditstop", "creditop");
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("credits.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Credits";
    }
}
