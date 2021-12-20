package me.pwo.evilprisoncore.gems;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.gems.api.GemsAPI;
import me.pwo.evilprisoncore.gems.api.GemsAPIImpl;
import me.pwo.evilprisoncore.gems.command.*;
import me.pwo.evilprisoncore.gems.manager.GemsManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class Gems implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private GemsManager gemsManager;
    private GemsAPI api;
    private Map<String, GemsCommand> commands;
    private boolean enabled;

    public Gems(EvilPrisonCore evilPrisonCore) {
        this.plugin = evilPrisonCore;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public GemsManager getGemsManager() {
        return gemsManager;
    }

    public GemsAPI getApi() {
        return api;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reload() {}

    public void enable() {
        this.gemsManager = new GemsManager(this);
        this.api = new GemsAPIImpl(this.gemsManager);
        registerEvents();
        registerCommands();
        this.enabled = true;
    }

    public void disable() {
        this.gemsManager.stopUpdating();
        this.gemsManager.savePlayerDataOnDisable();
        this.enabled = false;
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.MAGMA_CREAM)
                .filter(e -> e.getHand() != EquipmentSlot.OFF_HAND)
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        this.gemsManager.redeemGems(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new GemsGiveCommand(this));
        this.commands.put("pay", new GemsPayCommand(this));
        this.commands.put("remove", new GemsRemoveCommand(this));
        this.commands.put("set", new GemsSetCommand(this));
        this.commands.put("withdraw", new GemsWithdrawCommand(this));
        this.commands.put("help", new GemsHelpCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        this.gemsManager.sendInfoMessage(context.sender(), (OfflinePlayer)context.sender());
                        return;
                    }
                    GemsCommand gemsCommand = getCommand(context.rawArg(0));
                    if (gemsCommand != null) {
                        if (gemsCommand.canExecute(context.sender())) {
                            gemsCommand.execute(context.sender(), context.args().subList(1, context.args().size()));
                        } else {
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &fYou do not have permission to do that!");
                        }
                    } else {
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(context.rawArg(0));
                        this.gemsManager.sendInfoMessage(context.sender(), offlinePlayer);
                    }
                }).registerAndBind(this.plugin, "gems", "token");
        Commands.create()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0)
                        this.gemsManager.sendGemsTop(paramCommandContext.sender());
                }).registerAndBind(this.plugin, "gems", "tokentop");
    }

    public String getName() {
        return "Gems";
    }

    private GemsCommand getCommand(String paramString) {
        return this.commands.get(paramString.toLowerCase());
    }
}
