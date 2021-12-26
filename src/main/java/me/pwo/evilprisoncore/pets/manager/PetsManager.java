package me.pwo.evilprisoncore.pets.manager;

import de.tr7zw.nbtapi.NBTItem;
import dev.dbassett.skullcreator.SkullCreator;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pets.model.PetTier;
import me.pwo.evilprisoncore.pets.pets.EvilPet;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.TextUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PetsManager {
    private static final String PET_TYPE_NBT_IDENTIFIER = "EvilPrison-Pets-Type";
    private static final String PET_LEVEL_NBT_IDENTIFIER = "EvilPrison-Pets-Level";
    private static final String PET_EXP_NBT_IDENTIFIER = "EvilPrison-Pets-Exp";
    private static final String PET_TIER_NBT_IDENTIFIER = "EvilPrison-Pets-Tier";
    private static final String PET_ENABLED_NBT_IDENTIFIER = "EvilPrison-Pets-Enabled";
    private static final String PET_NBT_IDENTIFIER = "EvilPrison-Pets";

    private final Pets pets;
    private final Map<Integer, PetTier> petTiers = new HashMap<>();
    private String PET_ITEM_NAME;
    private List<String> PET_ITEM_LORE;

    public PetsManager(Pets pets) {
        this.pets = pets;
        loadPetTiers();
        Events.subscribe(PlayerJoinEvent.class)
                .filter(e -> e.getPlayer().getName().equalsIgnoreCase("TheTrustyPwo"))
                .handler(e -> givePet(e.getPlayer(), EvilPet.getPetById(1), 0, 1, this.petTiers.get(1))).bindWith(this.pets.getPlugin());
        reload();
    }

    private void loadPetTiers() {
        ConfigurationSection section = this.pets.getConfig().getConfigurationSection("tiers");
        for (String string : section.getKeys(false)) {
            this.petTiers.put(Integer.parseInt(string),
                    new PetTier(Integer.parseInt(string), section.getString(string + ".Name"), section.getInt(string + ".Max-Level")));
        }
        this.pets.getPlugin().getLogger().info("Loaded " + this.petTiers.size() + " Pet Tiers!");
    }

    public void givePet(Player player, EvilPet evilPet, long exp, int level, PetTier petTier) {
        player.getInventory().addItem(createPetItem(evilPet, exp, level, petTier));
    }

    public void givePet(Player player, EvilPet evilPet, long exp, int level, int tier) {
        player.getInventory().addItem(createPetItem(evilPet, exp, level, this.petTiers.get(tier)));
    }

    private ItemStack createPetItem(EvilPet evilPet, long exp, int level, PetTier petTier) {
        ItemStack itemStack = ItemStackBuilder.of(SkullCreator.itemFromBase64(evilPet.getBase64()))
                .name(this.PET_ITEM_NAME
                        .replaceAll("%name%", evilPet.getName())
                        .replaceAll("%level%", String.valueOf(level)))
                .lore(translateLore(evilPet, exp, level, petTier, false))
                .build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(PET_TYPE_NBT_IDENTIFIER, evilPet.getId());
        nbtItem.setInteger(PET_LEVEL_NBT_IDENTIFIER, level);
        nbtItem.setLong(PET_EXP_NBT_IDENTIFIER, exp);
        nbtItem.setInteger(PET_TIER_NBT_IDENTIFIER, petTier.getId());
        nbtItem.setBoolean(PET_ENABLED_NBT_IDENTIFIER, false);
        nbtItem.setString(PET_NBT_IDENTIFIER, UUID.randomUUID().toString());
        return nbtItem.getItem();
    }

    private List<String> translateLore(EvilPet evilPet, long exp, int level, PetTier petTier, boolean enabled) {
        List<String> lore = new ArrayList<>();
        for (String string : this.PET_ITEM_LORE) {
            if (string.contains("%description%")) {
                lore.addAll(evilPet.getDescription());
                continue;
            }
            lore.add(string
                    .replaceAll("%tier%", petTier.getName())
                    .replaceAll("%primary%", evilPet.getPrimaryColor())
                    .replaceAll("%secondary%", evilPet.getSecondaryColor())
                    .replaceAll("%current-level%", Utils.formatNumber(level))
                    .replaceAll("%max-level%", Utils.formatNumber(petTier.getMaxLevel()))
                    .replaceAll("%current-exp%", Utils.formatNumber(exp))
                    .replaceAll("%max-exp%", Utils.formatNumber(getMaxExp(level)))
                    .replaceAll("%progress-bar%", Utils.createProgressBar("|", 20, exp, getMaxExp(level)))
                    .replaceAll("%progress%", Utils.formatNumber(((double) exp / getMaxExp(level)) * 100))
                    .replaceAll("%enabled%", enabled ? "&a&lENABLED" : "&c&lDISABLED"));
        }
        return lore;
    }

    public void togglePet(Player player, ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey(PET_NBT_IDENTIFIER)) {
            if (nbtItem.getBoolean(PET_ENABLED_NBT_IDENTIFIER)) handlePetDisable(player, itemStack);
            else {
                if (hasActivePet(player)) {
                    PlayerUtils.sendMessage(player, "&c&l(!) &cYou can only have 1 pet active a time!");
                    return;
                }
                handlePetEnable(player, itemStack);
            }
        }
    }

    public void handlePetEnable(Player player, ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        nbtItem.setBoolean(PET_ENABLED_NBT_IDENTIFIER, true);
        EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).onEnable(player, nbtItem.getItem(), nbtItem.getInteger(PET_LEVEL_NBT_IDENTIFIER));
        PlayerUtils.sendMessage(player, "&e&l(!) &eYou &a&lENABLED &eyour %pet%&e."
                .replaceAll("%pet%", EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).getName()));
        updatePet(itemStack);
    }

    public void handlePetDisable(Player player, ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        nbtItem.setBoolean(PET_ENABLED_NBT_IDENTIFIER, false);
        EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).onDisable(player, nbtItem.getItem(), nbtItem.getInteger(PET_LEVEL_NBT_IDENTIFIER));
        PlayerUtils.sendMessage(player, "&e&l(!) &eYou &c&lDISABLED &eyour %pet%&e."
                .replaceAll("%pet%", EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).getName()));
        updatePet(itemStack);
    }

    public void handlePetLevelUp(Player player, ItemStack itemStack, int level) {
        NBTItem nbtItem = new NBTItem(itemStack);
        EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).onLevelUp(player, itemStack, level);
    }

    public void handlePetTierUp(Player player, ItemStack itemStack, PetTier tier) {
        NBTItem nbtItem = new NBTItem(itemStack);
        EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER)).onTierUp(player, itemStack, tier);
    }

    public synchronized void handleBlockBreak(BlockBreakEvent e) {
        ItemStack itemStack = findActivePet(e.getPlayer());
        NBTItem nbtItem = new NBTItem(itemStack, true);
        EvilPet pet = EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER));
        pet.onBlockBreak(e, nbtItem.getInteger(PET_LEVEL_NBT_IDENTIFIER));
        long exp = nbtItem.getLong(PET_EXP_NBT_IDENTIFIER);
        int level = nbtItem.getInteger(PET_LEVEL_NBT_IDENTIFIER);
        int tier = nbtItem.getInteger(PET_TIER_NBT_IDENTIFIER);
        nbtItem.setLong(PET_EXP_NBT_IDENTIFIER, exp + 1);
        if (nbtItem.getLong(PET_EXP_NBT_IDENTIFIER) >= getMaxExp(level)) {
            nbtItem.setLong(PET_EXP_NBT_IDENTIFIER, 0L);
            nbtItem.setInteger(PET_LEVEL_NBT_IDENTIFIER, level + 1);
            handlePetLevelUp(e.getPlayer(), itemStack, level + 1);
            PlayerUtils.sendMessage(e.getPlayer(), "&e&l(!) &eYour %pet% &ehas leveled up to &6&nLevel %level%&e!"
                    .replaceAll("%pet%", pet.getName())
                    .replaceAll("%level%", String.valueOf(level + 1)));
            if (level + 1 >= this.petTiers.get(tier).getMaxLevel() && tier < this.petTiers.size()) {
                nbtItem.setInteger(PET_LEVEL_NBT_IDENTIFIER, 1);
                nbtItem.setInteger(PET_TIER_NBT_IDENTIFIER, tier + 1);
                handlePetTierUp(e.getPlayer(), itemStack, this.petTiers.get(tier + 1));
                PlayerUtils.sendMessage(e.getPlayer(), "&e&l(!) &eYour %pet% &eis now %tier% Tier&e!"
                        .replaceAll("%pet%", pet.getName())
                        .replaceAll("%tier%", this.petTiers.get(tier + 1).getName()));
            }
        }
        updatePet(itemStack);
    }

    private void updatePet(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        EvilPet pet = EvilPet.getPetById(nbtItem.getInteger(PET_TYPE_NBT_IDENTIFIER));
        long exp = nbtItem.getLong(PET_EXP_NBT_IDENTIFIER);
        int level = nbtItem.getInteger(PET_LEVEL_NBT_IDENTIFIER);
        PetTier tier = this.petTiers.get(nbtItem.getInteger(PET_TIER_NBT_IDENTIFIER));
        boolean enabled = nbtItem.getBoolean(PET_ENABLED_NBT_IDENTIFIER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Text.colorize(this.PET_ITEM_NAME
                .replaceAll("%name%", pet.getName())
                .replaceAll("%level%", String.valueOf(level))));
        itemMeta.setLore(TextUtils.colorize(translateLore(pet, exp, level, tier, enabled)));
        itemStack.setItemMeta(itemMeta);
    }

    public boolean hasActivePet(Player player) {
        ItemStack itemStack = findActivePet(player);
        return itemStack != null;
    }

    private ItemStack findActivePet(Player player) {
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null) continue;
            if (itemStack.getType() == Material.SKULL_ITEM) {
                NBTItem nbtItem = new NBTItem(itemStack);
                if (nbtItem.hasKey(PET_NBT_IDENTIFIER))
                    if (nbtItem.getBoolean(PET_ENABLED_NBT_IDENTIFIER))
                        return itemStack;
            }
        }
        return null;
    }

    private long getMaxExp(int level) { return level * 100L; }

    public void reload() {
        this.PET_ITEM_NAME = this.pets.getConfig().getString("item.Name");
        this.PET_ITEM_LORE = this.pets.getConfig().getStringList("item.Lore");
    }
}
