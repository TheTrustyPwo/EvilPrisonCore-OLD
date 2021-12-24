package me.pwo.evilprisoncore.blocks;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.blocks.api.BlocksAPI;
import me.pwo.evilprisoncore.blocks.api.BlocksAPIImpl;
import me.pwo.evilprisoncore.blocks.blockrewards.BlockRewards;
import me.pwo.evilprisoncore.blocks.command.BlocksCommand;
import me.pwo.evilprisoncore.blocks.command.BlocksGiveCommand;
import me.pwo.evilprisoncore.blocks.command.BlocksRemoveCommand;
import me.pwo.evilprisoncore.blocks.command.BlocksSetCommand;
import me.pwo.evilprisoncore.blocks.manager.BlocksManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class Blocks implements EvilPrisonModules {
    private static Blocks instance;
    private final EvilPrisonCore plugin;
    private BlocksManager blocksManager;
    private BlocksAPI api;
    private Map<String, BlocksCommand> commands;
    private BlockRewards blockRewards;
    private boolean enabled;

    public static Blocks getInstance() {
        return instance;
    }

    public Blocks(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public BlocksManager getBlocksManager() {
        return blocksManager;
    }

    public BlocksAPI getApi() {
        return api;
    }

    public BlockRewards getBlockRewards() {
        return blockRewards;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reload() {}

    public void enable() {
        instance = this;
        this.blocksManager = new BlocksManager(this);
        this.api = new BlocksAPIImpl(this.blocksManager);
        registerEvents();
        registerCommands();
        this.blockRewards = new BlockRewards(this);
        this.plugin.loadModule(blockRewards);
        this.enabled = true;
    }

    public void disable() {
        this.blocksManager.stopUpdating();
        this.blocksManager.savePlayerDataOnDisable();
        this.enabled = false;
    }

    private void registerEvents() {
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> (e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.plugin.isPickaxeSupported(e.getPlayer().getItemInHand().getType())))
                .filter(e -> (WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch((region) -> region.getId().toLowerCase().startsWith("mine-"))))
                .handler(e -> this.blocksManager.giveBlocks(e.getPlayer(), 1L)).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new BlocksGiveCommand(this));
        this.commands.put("remove", new BlocksRemoveCommand(this));
        this.commands.put("set", new BlocksSetCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        this.blocksManager.sendInfoMessage(context.sender(), (OfflinePlayer)context.sender());
                        return;
                    }
                    BlocksCommand blocksCommand = this.commands.get(context.rawArg(0));
                    if (blocksCommand != null) {
                        if (blocksCommand.canExecute(context.sender())) {
                            blocksCommand.execute(context.sender(), context.args().subList(1, context.args().size()));
                        } else {
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cNo Permission");
                        }
                    } else {
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(context.rawArg(0));
                        this.blocksManager.sendInfoMessage(context.sender(), offlinePlayer);
                    }
                }).registerAndBind(this.plugin, "blocks", "block");
        Commands.create()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0)
                        this.blocksManager.sendBlocksTop(paramCommandContext.sender());
                }).registerAndBind(this.plugin, "blockstop", "blocktop");
    }

    public String getName() {
        return "Blocks";
    }
}
