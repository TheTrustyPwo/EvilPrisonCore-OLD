package me.pwo.evilprisoncore.pickaxe.pickaxelevels;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.api.PickaxeLevelsAPI;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.api.PickaxeLevelsAPIImpl;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.model.PickaxeLevel;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class PickaxeLevels implements EvilPrisonModules {
    private static final String NBT_TAG_IDENTIFIER = "EvilPrison-PickaxeLevel";
    private final EvilPrisonCore plugin;
    private FileConfiguration config;
    private Map<Integer, PickaxeLevel> pickaxeLevels;
    private Map<String, String> messages;
    private PickaxeLevel defaultLevel;
    private PickaxeLevel maxLevel;
    private PickaxeLevelsAPI api;
    private boolean enabled;

    public FileConfiguration getConfig() {
        return this.config;
    }

    public PickaxeLevelsAPI getApi() {
        return this.api;
    }

    public EvilPrisonCore getPlugin() {
        return this.plugin;
    }

    public PickaxeLevels(EvilPrisonCore paramUltraPrisonCore) {
        this.plugin = paramUltraPrisonCore;
    }

    private void loadMessages() {
        this.messages = new HashMap<>();
        for (String str : getConfig().getConfigurationSection("messages").getKeys(false))
            this.messages.put(str.toLowerCase(), Text.colorize(getConfig().getString("messages." + str)));
    }

    private void loadPickaxeLevels() {
        this.pickaxeLevels = new LinkedHashMap<>();
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("levels");
        if (configurationSection == null)
            return;
        for (String str1 : configurationSection.getKeys(false)) {
            int level = Integer.parseInt(str1);
            long blocksRequired = getConfig().getLong("levels." + str1 + ".blocks_required");
            List<String> rewards = getConfig().getStringList("levels." + str1 + ".rewards");
            PickaxeLevel pickaxeLevel = new PickaxeLevel(level, blocksRequired, rewards);
            if (level == 1)
                this.defaultLevel = pickaxeLevel;
            this.pickaxeLevels.put(level, pickaxeLevel);
            this.maxLevel = pickaxeLevel;
            this.plugin.getLogger().info("Loaded Pickaxe Level " + level);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reload() {
        this.plugin.getFileUtils().getConfig("pickaxe-levels.yml").reload();
        loadMessages();
        loadPickaxeLevels();
    }

    public void enable() {
        this.enabled = true;
        this.config = this.plugin.getFileUtils().getConfig("pickaxe-levels.yml").copyDefaults(true).save().get();
        loadPickaxeLevels();
        loadMessages();
        registerCommands();
        registerListeners();
        this.api = new PickaxeLevelsAPIImpl(this);
    }

    private void registerListeners() {
        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(paramBlockBreakEvent -> WorldGuardWrapper.getInstance().getRegions(paramBlockBreakEvent.getBlock().getLocation()).stream().anyMatch((region) -> region.getId().toLowerCase().startsWith("mine-")))
                .filter(paramBlockBreakEvent -> (paramBlockBreakEvent.getPlayer().getGameMode() == GameMode.SURVIVAL && paramBlockBreakEvent.getPlayer().getItemInHand() != null && getPlugin().isPickaxeSupported(paramBlockBreakEvent.getPlayer().getItemInHand().getType())))
                .handler(paramBlockBreakEvent -> {
                    PickaxeLevel pickaxeLevel1 = getPickaxeLevel(paramBlockBreakEvent.getPlayer().getItemInHand());
                    PickaxeLevel pickaxeLevel2 = getNextPickaxeLevel(pickaxeLevel1);
                    if (pickaxeLevel2 != null && this.plugin.getEnchants().getEnchantsManager().getBlocksBroken(paramBlockBreakEvent.getPlayer().getItemInHand()) >= pickaxeLevel2.getBlocksRequired()) {
                        pickaxeLevel2.giveRewards(paramBlockBreakEvent.getPlayer());
                        paramBlockBreakEvent.getPlayer().setItemInHand(setPickaxeLevel(paramBlockBreakEvent.getPlayer().getItemInHand(), pickaxeLevel2));
                        PlayerUtils.sendMessage(paramBlockBreakEvent.getPlayer(), getMessage("pickaxe-level-up").replace("%level%", String.valueOf(pickaxeLevel2.getLevel())));
                    }
                }).bindWith(this.plugin);
        Events.subscribe(PlayerItemHeldEvent.class)
                .handler(paramPlayerItemHeldEvent -> {
                    ItemStack itemStack = paramPlayerItemHeldEvent.getPlayer().getInventory().getItem(paramPlayerItemHeldEvent.getNewSlot());
                    if (itemStack != null && getPlugin().isPickaxeSupported(itemStack.getType()) && getPickaxeLevel(itemStack) == null)
                        paramPlayerItemHeldEvent.getPlayer().getInventory().setItem(paramPlayerItemHeldEvent.getNewSlot(), addDefaultPickaxeLevel(itemStack));
                }).bindWith(this.plugin);
    }

    public PickaxeLevel getNextPickaxeLevel(PickaxeLevel paramPickaxeLevel) {
        if (paramPickaxeLevel == null || paramPickaxeLevel == this.maxLevel)
            return null;
        return this.pickaxeLevels.get(paramPickaxeLevel.getLevel() + 1);
    }

    public void disable() {
        this.enabled = false;
    }

    public String getName() {
        return "Pickaxe Levels";
    }

    private void registerCommands() {}

    public String getMessage(String paramString) {
        return this.messages.get(paramString.toLowerCase());
    }

    public PickaxeLevel getPickaxeLevel(ItemStack paramItemStack) {
        if (paramItemStack == null || !getPlugin().isPickaxeSupported(paramItemStack.getType()))
            return null;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (!nBTItem.hasKey(NBT_TAG_IDENTIFIER))
            return this.defaultLevel;
        return this.pickaxeLevels.get(nBTItem.getInteger(NBT_TAG_IDENTIFIER));
    }

    public ItemStack setPickaxeLevel(ItemStack paramItemStack, PickaxeLevel paramPickaxeLevel) {
        if (paramPickaxeLevel.getLevel() <= 0 || paramPickaxeLevel.getLevel() > this.maxLevel.getLevel())
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (!nBTItem.hasKey(NBT_TAG_IDENTIFIER))
            nBTItem.setInteger(NBT_TAG_IDENTIFIER, 0);
        nBTItem.setInteger(NBT_TAG_IDENTIFIER, paramPickaxeLevel.getLevel());
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(nBTItem.getItem());
        paramItemStack = itemStackBuilder.build();
        this.plugin.getEnchants().getEnchantsManager().updatePickaxe(paramItemStack);
        return paramItemStack;
    }

    private ItemStack addDefaultPickaxeLevel(ItemStack paramItemStack) {
        return setPickaxeLevel(paramItemStack, this.defaultLevel);
    }

    public ItemStack findPickaxe(Player paramPlayer) {
        for (ItemStack itemStack : paramPlayer.getInventory()) {
            if (itemStack == null)
                continue;
            if (getPlugin().isPickaxeSupported(itemStack.getType()))
                return itemStack;
        }
        return null;
    }

    public String getProgressBar(ItemStack paramItemStack) {
        PickaxeLevel pickaxeLevel1 = getPickaxeLevel(paramItemStack);
        PickaxeLevel pickaxeLevel2 = getNextPickaxeLevel(pickaxeLevel1);
        if (pickaxeLevel2 != null) {
            long l1 = pickaxeLevel2.getBlocksRequired() - pickaxeLevel1.getBlocksRequired();
            double d = l1 / 20.0D;
            long l2 = this.plugin.getEnchants().getEnchantsManager().getBlocksBroken(paramItemStack) - pickaxeLevel1.getBlocksRequired();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b = 0; b < 20; b++) {
                if (l2 >= d * (b + 1)) {
                    stringBuilder.append("&a:");
                } else {
                    stringBuilder.append("&c:");
                }
            }
            return Text.colorize(stringBuilder.toString());
        }
        return Text.colorize("&a::::::::::::::::::::");
    }
}
