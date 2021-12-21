package me.pwo.evilprisoncore.pickaxe.pickaxerenametoken;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.api.PickaxeRenameTokenAPI;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.api.PickaxeRenameTokenAPIImpl;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command.PickaxeRenameTokenCommand;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command.PickaxeRenameTokenGiveCommand;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PickaxeRenameToken implements EvilPrisonModules {
    private static final String RENAME_TOKEN_NBT_IDENTIFIER = "EvilPrison-Pickaxe-RenameToken";
    private static PickaxeRenameToken instance;
    private final EvilPrisonCore plugin;
    private Map<String, PickaxeRenameTokenCommand> commands;
    private PickaxeRenameTokenAPI api;
    private boolean enabled;

    public static PickaxeRenameToken getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public PickaxeRenameTokenAPI getApi() {
        return api;
    }

    public PickaxeRenameToken(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.api = new PickaxeRenameTokenAPIImpl(this);
        registerCommands();
        registerEvents();
        this.enabled = true;
    }

    private void registerCommands() {
        this.commands = new HashMap<>();
        this.commands.put("give", new PickaxeRenameTokenGiveCommand(this));
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0 && context.sender() instanceof Player) return;
                    PickaxeRenameTokenCommand pickaxeRenameTokenCommand = this.commands.get(context.rawArg(0).toLowerCase());
                    if (pickaxeRenameTokenCommand != null) {
                        if (pickaxeRenameTokenCommand.canExecute(context.sender()))
                            pickaxeRenameTokenCommand.execute(context.sender(), context.args().subList(1, context.args().size()));
                        else
                            PlayerUtils.sendMessage(context.sender(), "&c&l(!) &fYou do not have permission to do that!");
                    }
                }).registerAndBind(this.plugin, "renametoken", "renametokens", "rt");
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getItem() == createRenameTokenItem(e.getItem().getAmount()))
                .handler(e -> {
                    ItemStack pickaxe = Enchants.getInstance().getEnchantsManager().findPickaxe(e.getPlayer());
                    PlayerUtils.sendMessage(e.getPlayer(), "&aEnter the new name for your pickaxe.");
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer() == e.getPlayer())
                            .handler(event -> {
                                PlayerUtils.sendMessage(e.getPlayer(), "&aRename successful!");
                                ItemStackBuilder.of(pickaxe).name(event.getMessage());
                            }).bindWith(this.plugin);
                });
    }

    public ItemStack createRenameTokenItem(int amount) {
        ItemStack itemStack = ItemStackBuilder.of(Material.NAME_TAG)
                .name("&6&lRename Token &f&o(Right Click)")
                .amount(amount).build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setUUID(RENAME_TOKEN_NBT_IDENTIFIER, UUID.randomUUID());
        nbtItem.applyNBT(itemStack);
        return itemStack;
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
        return "Pickaxe Rename Tokens";
    }
}
