package me.pwo.evilprisoncore.pickaxe;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.PickaxeLevels;

public class Pickaxe implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private PickaxeLevels pickaxeLevels;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public PickaxeLevels getPickaxeLevels() {
        return pickaxeLevels;
    }

    public Pickaxe(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        pickaxeLevels = new PickaxeLevels(plugin);
        plugin.loadModule(pickaxeLevels);
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
