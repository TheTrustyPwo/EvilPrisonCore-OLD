package me.pwo.evilprisoncore.tokens;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.tokens.api.TokensAPI;
import me.pwo.evilprisoncore.tokens.api.TokensAPIImpl;
import me.pwo.evilprisoncore.tokens.command.*;
import me.pwo.evilprisoncore.tokens.manager.TokensManager;
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

public class Tokens implements EvilPrisonModules {
    private final EvilPrisonCore plugin;
    private TokensManager tokensManager;
    private TokensAPI api;
    private Map<String, TokensCommand> commands;
    private boolean enabled;

    public Tokens(EvilPrisonCore evilPrisonCore) {
        this.plugin = evilPrisonCore;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public TokensManager getTokensManager() {
        return tokensManager;
    }

    public TokensAPI getApi() {
        return api;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reload() {}

    public void enable() {
        this.tokensManager = new TokensManager(this);
        this.api = new TokensAPIImpl(this.tokensManager);
        registerEvents();
        registerCommands();
        this.enabled = true;
    }

    public void disable() {
        this.tokensManager.stopUpdating();
        this.tokensManager.savePlayerDataOnDisable();
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
                        this.tokensManager.redeemTokens(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new TokensGiveCommand(this));
        this.commands.put("pay", new TokensPayCommand(this));
        this.commands.put("remove", new TokensRemoveCommand(this));
        this.commands.put("set", new TokensSetCommand(this));
        this.commands.put("withdraw", new TokensWithdrawCommand(this));
        this.commands.put("help", new TokensHelpCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) {
                        this.tokensManager.sendInfoMessage(context.sender(), (OfflinePlayer)context.sender());
                        return;
                    }
                    TokensCommand tokensCommand = getCommand(context.rawArg(0));
                    if (tokensCommand != null) {
                        if (tokensCommand.canExecute(context.sender())) {
                            tokensCommand.execute(context.sender(), context.args().subList(1, context.args().size()));
                        } else {
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &fYou do not have permission to do that!");
                        }
                    } else {
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(context.rawArg(0));
                        this.tokensManager.sendInfoMessage(context.sender(), offlinePlayer);
                    }
                }).registerAndBind(this.plugin, "tokens", "token");
        Commands.create()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0)
                        this.tokensManager.sendTokensTop(paramCommandContext.sender());
                }).registerAndBind(this.plugin, "tokenstop", "tokentop");
    }

    public String getName() {
        return "Tokens";
    }

    private TokensCommand getCommand(String paramString) {
        return this.commands.get(paramString.toLowerCase());
    }
}
