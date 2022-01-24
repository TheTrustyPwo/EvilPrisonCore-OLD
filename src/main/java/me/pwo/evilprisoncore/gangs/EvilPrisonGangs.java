package me.pwo.evilprisoncore.gangs;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import me.pwo.evilprisoncore.gangs.commands.impl.*;
import me.pwo.evilprisoncore.gangs.manager.GangManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class EvilPrisonGangs implements EvilPrisonModule {
    private final EvilPrisonCore plugin;
    private GangManager gangManager;
    private FileConfiguration gangsConfig;
    private Map<String, GangsCommand> commands;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public GangManager getGangManager() {
        return gangManager;
    }

    public EvilPrisonGangs(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.gangsConfig = this.plugin.getFileUtils().getConfig("gangs.yml").copyDefaults(true).save().get();
        this.gangManager = new GangManager(this);
        registerEvents();
        registerCommands();
        this.enabled = true;
    }

    private void registerEvents() {
        Events.subscribe(AsyncPlayerChatEvent.class)
                .filter(e -> this.gangManager.hasGangChatEnabled(e.getPlayer()))
                .handler(e -> {
                    e.setCancelled(true);
                    this.gangManager.gangChat(e.getPlayer(), e.getMessage());
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("create", new GangCreateCommand(this));
        this.commands.put("disband", new GangDisbandCommand(this));
        this.commands.put("invite", new GangInviteCommand(this));
        this.commands.put("accept", new GangAcceptCommand(this));
        this.commands.put("leave", new GangLeaveCommand(this));
        this.commands.put("kick", new GangKickCommand(this));
        this.commands.put("rename", new GangRenameCommand(this));
        this.commands.put("chat", new GangChatCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {

                        return;
                    }
                    GangsCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) {
                        if (command.canExecute(context.sender())) {
                            command.execute(context.sender(), context.args().subList(1, context.args().size()));
                        } else {
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                        }
                    } else {

                    }
                }).registerAndBind(this.plugin, "gangs", "gang");
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0)
                        this.gangManager.sendGangTop(context.sender());
                }).registerAndBind(this.plugin, "gangstop", "gangtop");
    }

    @Override
    public void disable() {
        this.gangManager.saveDataOnDisable();
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("gangs.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Gangs";
    }
}
