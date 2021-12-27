package me.pwo.evilprisoncore.privatemines;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.privatemines.manager.PrivateMinesManager;
import me.pwo.evilprisoncore.privatemines.manager.PrivateMinesWorldManager;
import org.bukkit.configuration.file.FileConfiguration;

public class PrivateMines implements EvilPrisonModules {
    private static PrivateMines instance;
    private final EvilPrisonCore plugin;
    private PrivateMinesManager privateMinesManager;
    private PrivateMinesWorldManager privateMinesWorldManager;
    private FileConfiguration privateMinesConfig;
    private boolean enabled;

    public static PrivateMines getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public PrivateMinesManager getPrivateMinesManager() {
        return privateMinesManager;
    }

    public PrivateMinesWorldManager getPrivateMinesWorldManager() {
        return privateMinesWorldManager;
    }

    public PrivateMines(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.privateMinesConfig = this.plugin.getFileUtils().getConfig("private-mines.yml").copyDefaults(true).save().get();
        this.privateMinesManager = new PrivateMinesManager(this);
        this.privateMinesWorldManager = new PrivateMinesWorldManager(this);
        registerEvents();
        this.enabled = true;
    }

    private void registerEvents() {

    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("private-mines.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Private Mines";
    }
}
