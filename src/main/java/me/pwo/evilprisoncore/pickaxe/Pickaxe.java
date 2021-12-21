package me.pwo.evilprisoncore.pickaxe;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.PickaxeLevels;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.PickaxeRenameToken;

public class Pickaxe implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private PickaxeLevels pickaxeLevels;
    private PickaxeRenameToken pickaxeRenameToken;
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

    public Pickaxe(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        pickaxeLevels = new PickaxeLevels(plugin);
        pickaxeRenameToken = new PickaxeRenameToken(plugin);
        plugin.loadModule(pickaxeLevels);
        plugin.loadModule(pickaxeRenameToken);
        this.enabled = true;
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
        return "Pickaxe";
    }
}
