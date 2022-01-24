package me.pwo.evilprisoncore.privatemines;

import me.lucko.helper.Commands;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.privatemines.api.PrivateMinesAPI;
import me.pwo.evilprisoncore.privatemines.api.PrivateMinesAPIImpl;
import me.pwo.evilprisoncore.privatemines.command.*;
import me.pwo.evilprisoncore.privatemines.gui.MinesGUI;
import me.pwo.evilprisoncore.privatemines.manager.PrivateMinesManager;
import me.pwo.evilprisoncore.privatemines.manager.PrivateMinesWorldManager;
import me.pwo.evilprisoncore.utils.Direction;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PrivateMines implements EvilPrisonModule {
    private static PrivateMines instance;
    private final EvilPrisonCore plugin;
    private PrivateMinesManager privateMinesManager;
    private PrivateMinesWorldManager privateMinesWorldManager;
    private FileConfiguration privateMinesConfig;
    private PrivateMinesAPI privateMinesAPI;
    private Map<String, PrivateMinesCommand> commands;
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

    public FileConfiguration getConfig() {
        return privateMinesConfig;
    }

    public PrivateMinesAPI getApi() {
        return privateMinesAPI;
    }

    public PrivateMines(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.privateMinesConfig = this.plugin.getFileUtils().getConfig("private-mines.yml").copyDefaults(true).save().get();
        this.privateMinesWorldManager = new PrivateMinesWorldManager(this);
        this.privateMinesManager = new PrivateMinesManager(this);
        this.privateMinesAPI = new PrivateMinesAPIImpl(this);
        registerCommands();
        this.enabled = true;
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("go", new PrivateMinesGoCommand(this));
        this.commands.put("tp", new PrivateMinesGoCommand(this));
        this.commands.put("teleport", new PrivateMinesGoCommand(this));
        this.commands.put("setpublic", new PrivateMinesSetPublicCommand(this));
        this.commands.put("create", new PrivateMinesCreateCommand(this));
        this.commands.put("setsize", new PrivateMinesSetSizeCommand(this));
        this.commands.put("reset", new PrivateMinesResetCommand(this));
        this.commands.put("forcesave", new PrivateMinesForceSaveCommand(this));
        Commands.create()
                .tabHandler(context -> {
                    if (context.args().size() == 1) {
                        return this.commands.keySet().stream()
                                .filter(cmd -> this.commands.get(cmd).canExecute(context.sender())).collect(Collectors.toList());
                    }
                    PrivateMinesCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) if (command.canExecute(context.sender()))
                        return command.onTabComplete(context.sender(), context.args().subList(1, context.args().size()));
                    return null;
                }).handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        (new MinesGUI((Player) context.sender())).open();
                        return;
                    }
                    PrivateMinesCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) if (command.canExecute(context.sender()))
                        command.execute(context.sender(), context.args().subList(1, context.args().size()));
                    else PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                    else (new MinesGUI((Player) context.sender())).open();
                }).registerAndBind(this.plugin, "privatemines", "privatemine", "pmines", "pmine", "mines", "mine");
    }


    public int getMinesDistance() {
        return getConfig().getInt("Distance", 0);
    }

    public Direction getMineDirection() {
        return Direction.valueOf(getConfig().getString("Direction", "NORTH"));
    }

    public void saveMinesDistance(int distance) {
        getConfig().set("Distance", distance);
        getPlugin().getFileUtils().getConfig("private-mines.yml").save();
    }

    public void saveMineDirection(Direction direction) {
        getConfig().set("Direction", direction.toString());
        getPlugin().getFileUtils().getConfig("private-mines.yml").save();
    }

    @Override
    public void disable() {
        this.privateMinesManager.saveAllMines();
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
