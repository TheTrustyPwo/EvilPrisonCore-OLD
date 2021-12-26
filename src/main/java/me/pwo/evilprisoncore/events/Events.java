package me.pwo.evilprisoncore.events;

import me.lucko.helper.Commands;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.events.events.RareBlock;
import me.pwo.evilprisoncore.events.gui.EventsGUI;
import org.bukkit.configuration.file.FileConfiguration;

public class Events implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private FileConfiguration eventsConfig;
    private RareBlock rareBlock;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public FileConfiguration getConfig() {
        return eventsConfig;
    }

    public Events(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.eventsConfig = this.plugin.getFileUtils().getConfig("events.yml").copyDefaults(true).save().get();
        this.rareBlock = new RareBlock(this);
        registerCommands();
        this.enabled = true;
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(context -> (new EventsGUI(context.sender())).open()).registerAndBind(this.plugin, "event", "events");
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("events.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Events";
    }
}
