package me.pwo.evilprisoncore.gangs;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.gangs.commands.GangsCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class Gangs implements EvilPrisonModules {
    private EvilPrisonCore plugin;
    private FileConfiguration gangsConfig;
    private Map<String, GangsCommand> commands;
    private boolean enabled;

    public Gangs(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.gangsConfig = this.plugin.getFileUtils().getConfig("gangs.yml").copyDefaults(true).save().get();
        this.enabled = true;
    }

    @Override
    public void disable() {

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
