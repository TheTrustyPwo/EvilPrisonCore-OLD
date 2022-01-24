package me.pwo.evilprisoncore.pickaxe.pickaxerenametoken;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.api.PickaxeRenameTokenAPI;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.api.PickaxeRenameTokenAPIImpl;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command.PickaxeRenameTokenCommand;
import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.command.PickaxeRenameTokenGiveCommand;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class PickaxeRenameToken implements EvilPrisonModule {
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
        Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
                .filter(e -> e.getPlayer().getItemInHand().isSimilar(createRenameTokenItem(e.getItem().getAmount())))
                .filter(e -> e.getHand() != EquipmentSlot.OFF_HAND)
                .handler(e -> {
                    if (e.getPlayer().getItemInHand().getAmount() - 1 == 0) e.getPlayer().setItemInHand(null);
                    else e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
                    ItemStack pickaxe = Enchants.getInstance().getEnchantsManager().findPickaxe(e.getPlayer());
                    PlayerUtils.sendMessage(e.getPlayer(), "&aEnter the new name for your pickaxe! &e(Supports color codes: &4&k;;&6&l&n&oEVIL&4&k;;&r&e)", true);
                    Events.subscribe(AsyncPlayerChatEvent.class)
                            .expireAfter(1)
                            .filter(event -> event.getPlayer() == e.getPlayer())
                            .handler(event -> {
                                e.setCancelled(true);
                                PlayerUtils.sendMessage(e.getPlayer(), "&aRename successful!", true);
                                ItemStackBuilder.of(pickaxe).name(event.getMessage());
                            }).bindWith(this.plugin);
                }).bindWith(this.plugin);
    }

    public ItemStack createRenameTokenItem(int amount) {
        ItemStack itemStack = ItemStackBuilder.of(Material.NAME_TAG)
                .name("&6&lRename Token &f&o(Right Click)")
                .amount(amount)
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS).build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setByte(RENAME_TOKEN_NBT_IDENTIFIER, (byte) 0);
        nbtItem.applyNBT(itemStack);
        return itemStack;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {}

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Pickaxe Rename Tokens";
    }
}
