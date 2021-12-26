package me.pwo.evilprisoncore.multipliers;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.multipliers.manager.MultipliersManager;
import org.bukkit.configuration.file.FileConfiguration;

public class Multipliers implements EvilPrisonModules {
    private static Multipliers instance;
    private final EvilPrisonCore plugin;
    private MultipliersManager multipliersManager;
    private FileConfiguration multipliersConfig;
    private boolean enabled;

    public static Multipliers getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public MultipliersManager getMultipliersManager() {
        return multipliersManager;
    }

    public FileConfiguration getConfig() {
        return multipliersConfig;
    }

    public Multipliers(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.multipliersConfig = this.plugin.getFileUtils().getConfig("multipliers.yml").copyDefaults(true).save().get();
        this.multipliersManager = new MultipliersManager(this);
        registerEvents();
        registerCommands();
        this.enabled = true;
    }

    private void registerEvents() {

    }

    private void registerCommands() {

    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("multipliers.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Multipliers";
    }
}
