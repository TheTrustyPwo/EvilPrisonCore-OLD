package me.pwo.evilprisoncore.multipliers;

import me.lucko.helper.Commands;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.multipliers.api.MultipliersAPI;
import me.pwo.evilprisoncore.multipliers.api.MultipliersAPIImpl;
import me.pwo.evilprisoncore.multipliers.command.MultipliersCommand;
import me.pwo.evilprisoncore.multipliers.command.MultipliersGiveCommand;
import me.pwo.evilprisoncore.multipliers.gui.MultipliersGUI;
import me.pwo.evilprisoncore.multipliers.manager.MultipliersManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Multipliers implements EvilPrisonModule {
    private static Multipliers instance;
    private final EvilPrisonCore plugin;
    private MultipliersManager multipliersManager;
    private MultipliersAPI multipliersAPI;
    private FileConfiguration multipliersConfig;
    private Map<String, MultipliersCommand> commands;
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
        this.commands = new HashMap<>();
        this.commands.put("give", new MultipliersGiveCommand(this));
        Commands.create()
                .tabHandler(context -> {
                    if (context.args().size() == 1) {
                        return this.commands.keySet().stream()
                                .filter(cmd -> this.commands.get(cmd).canExecute(context.sender())).collect(Collectors.toList());
                    }
                    MultipliersCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) if (command.canExecute(context.sender()))
                        return command.onTabComplete(context.sender(), context.args().subList(1, context.args().size()));
                    return null;
                }).handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        (new MultipliersGUI((Player) context.sender())).open();
                        return;
                    }
                    MultipliersCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) if (command.canExecute(context.sender()))
                        command.execute(context.sender(), context.args().subList(1, context.args().size()));
                    else PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                    else (new MultipliersGUI((Player) context.sender())).open();
                }).registerAndBind(this.plugin, "multipliers", "multiplier", "multis", "multi");
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
