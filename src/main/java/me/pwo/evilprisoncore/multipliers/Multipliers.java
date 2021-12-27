package me.pwo.evilprisoncore.multipliers;

import me.lucko.helper.Commands;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.multipliers.api.MultipliersAPI;
import me.pwo.evilprisoncore.multipliers.api.MultipliersAPIImpl;
import me.pwo.evilprisoncore.multipliers.manager.MultipliersManager;
import me.pwo.evilprisoncore.multipliers.model.PlayerMultiplier;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class Multipliers implements EvilPrisonModules {
    private static Multipliers instance;
    private final EvilPrisonCore plugin;
    private MultipliersManager multipliersManager;
    private MultipliersAPI multipliersAPI;
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

    public MultipliersAPI getApi() {
        return multipliersAPI;
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
        this.multipliersAPI = new MultipliersAPIImpl(this);
        registerCommands();
        this.enabled = true;
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    PlayerMultiplier multi = this.multipliersManager.getPlayerMultiplier(context.sender());
                    PlayerUtils.sendMessage(context.sender(),
                            "a" + multi.getMultiplierSet().getMoneyMulti().getMultiplier()
                                    + multi.getMultiplierSet().getTokenMulti().getMultiplier() +
                                    multi.getMultiplierSet().getGemsMulti().getMultiplier() +
                                    multi.getMultiplierSet().getExpMulti().getMultiplier());
                }).registerAndBind(this.plugin, "multiplier", "multi");
    }

    @Override
    public void disable() {
        this.multipliersManager.saveAllMultipliers();
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
