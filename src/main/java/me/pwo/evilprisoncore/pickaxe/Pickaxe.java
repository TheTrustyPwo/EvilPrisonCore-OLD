package me.pwo.evilprisoncore.pickaxe;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.pickaxe.pickaxeboosters.PickaxeBoosters;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.PickaxeLevels;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.PickaxeRenameToken;
import org.bukkit.configuration.file.FileConfiguration;

public class Pickaxe implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private PickaxeLevels pickaxeLevels;
    private PickaxeRenameToken pickaxeRenameToken;
    private PickaxeBoosters pickaxeBoosters;
    private FileConfiguration pickaxeConfig;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public PickaxeLevels getPickaxeLevels() {
        return pickaxeLevels;
    }

    public PickaxeRenameToken getPickaxeRenameToken() {
        return pickaxeRenameToken;
    }

    public PickaxeBoosters getPickaxeBoosters() {
        return pickaxeBoosters;
    }

    public FileConfiguration getConfig() {
        return pickaxeConfig;
    }

    public Pickaxe(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.pickaxeConfig = this.plugin.getFileUtils().getConfig("pickaxe.yml").copyDefaults(true).save().get();
        pickaxeLevels = new PickaxeLevels(plugin);
        pickaxeRenameToken = new PickaxeRenameToken(plugin);
        pickaxeBoosters = new PickaxeBoosters(this);
        plugin.loadModule(pickaxeLevels);
        plugin.loadModule(pickaxeRenameToken);
        plugin.loadModule(pickaxeBoosters);
        this.enabled = true;
    }

    @Override
    public void disable() {

        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("pickaxe.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Pickaxe";
    }
}
