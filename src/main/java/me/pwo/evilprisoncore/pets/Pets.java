package me.pwo.evilprisoncore.pets;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import org.bukkit.configuration.file.FileConfiguration;

public class Pets implements EvilPrisonModules {
    private Pets instance;
    private EvilPrisonCore plugin;
    private FileConfiguration petsConfig;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public FileConfiguration getConfig() {
        return petsConfig;
    }

    public Pets getInstance() {
        return instance;
    }

    public Pets(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.petsConfig = this.plugin.getFileUtils().getConfig("pets.yml").copyDefaults(true).save().get();
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("pets.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return null;
    }
}
