package me.pwo.evilprisoncore.pets;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.pets.command.PetsCommand;
import me.pwo.evilprisoncore.pets.command.PetsGiveCommand;
import me.pwo.evilprisoncore.pets.gui.PetsGUI;
import me.pwo.evilprisoncore.pets.manager.PetsManager;
import me.pwo.evilprisoncore.pets.pets.EvilPet;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class Pets implements EvilPrisonModule {
    private static Pets instance;
    private final EvilPrisonCore plugin;
    private PetsManager petsManager;
    private FileConfiguration petsConfig;
    private Map<String, PetsCommand> commands;
    private boolean enabled;

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public PetsManager getPetsManager() {
        return petsManager;
    }

    public FileConfiguration getConfig() {
        return petsConfig;
    }

    public static Pets getInstance() {
        return instance;
    }

    public Pets(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.petsConfig = this.plugin.getFileUtils().getConfig("pets.yml").copyDefaults(true).save().get();
        this.petsManager = new PetsManager(this);
        registerEvents();
        registerCommands();
        EvilPet.loadDefaultPets();
        this.enabled = true;
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.SKULL_ITEM)
                .filter(e -> e.getHand() != EquipmentSlot.OFF_HAND)
                .handler(e -> this.petsManager.togglePet(e.getPlayer(), e.getPlayer().getItemInHand())).bindWith(this.plugin);
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> this.petsManager.hasActivePet(e.getPlayer()))
                .filter(e -> (e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && getPlugin().isPickaxeSupported(e.getPlayer().getItemInHand().getType())))
                .filter(e -> (WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch((region) -> region.getId().toLowerCase().startsWith("mine-"))))
                .handler(e -> this.petsManager.handleBlockBreak(e)).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new PetsGiveCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        (new PetsGUI((Player) context.sender())).open();
                        return;
                    }
                    PetsCommand command = this.commands.get(context.rawArg(0));
                    if (command != null) {
                        if (command.canExecute(context.sender()))
                            command.execute(context.sender(), context.args().subList(1, context.args().size()));
                        else PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                    } else (new PetsGUI((Player) context.sender())).open();
                }).registerAndBind(this.plugin, "pet", "pets");
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
        return "Pets";
    }
}
