package me.pwo.evilprisoncore.enchants.manager;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import me.pwo.evilprisoncore.enchants.gui.DisenchantGUI;
import me.pwo.evilprisoncore.enchants.gui.EnchantGUI;
import me.pwo.evilprisoncore.pickaxe.pickaxelevels.model.PickaxeLevel;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantsManager {
    private static final String BLOCK_BROKEN_NBT_IDENTIFIER = "EvilPrison-Pickaxe-BlocksBroken";
    private static final String ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX = "EvilPrison-Pickaxe-Enchant-";
    private static final String PICKAXE_ID_NBT_IDENTIFIER = "EvilPrison-Pickaxe-ID";
    private final Enchants enchants;
    private List<String> ENCHANT_GUI_ITEM_LORE;
    private List<String> DISENCHANT_GUI_ITEM_LORE;
    private List<String> PICKAXE_LORE;
    private boolean openEnchantMenuOnRightClickBlock;
    private boolean allowEnchantsOutside;
    private double refundPercentage;
    private final List<UUID> lockedPlayers;
    private boolean firstJoinPickaxeEnabled;
    private Material firstJoinPickaxeMaterial;
    private List<String> firstJoinPickaxeEnchants;
    private String firstJoinPickaxeName;

    public boolean isAllowEnchantsOutside() {
        return this.allowEnchantsOutside;
    }

    public double getRefundPercentage() {
        return this.refundPercentage;
    }

    public boolean isFirstJoinPickaxeEnabled() {
        return this.firstJoinPickaxeEnabled;
    }

    public EnchantsManager(Enchants enchants) {
        this.enchants = enchants;
        this.lockedPlayers = new ArrayList<>();
        reload();
    }

    public HashMap<EvilPrisonEnchantment, Integer> getPlayerEnchants(ItemStack itemStack) {
        HashMap<EvilPrisonEnchantment, Integer> hashMap = new HashMap<>();
        for (EvilPrisonEnchantment enchantment : EvilPrisonEnchantment.all()) {
            int level = getEnchantLevel(itemStack, enchantment.getId());
            if (level == 0) continue;
            hashMap.put(enchantment, level);
        }
        return hashMap;
    }

    public ItemStack findPickaxe(Player player) {
        for (ItemStack itemStack : player.getInventory())
            if (itemStack.getType() == Material.DIAMOND_PICKAXE) return itemStack;
        return null;
    }

    public void updatePickaxe(ItemStack itemStack) {
        applyLoreToPickaxe(itemStack);
    }

    private void applyLoreToPickaxe(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        PickaxeLevel currentLevel = this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getPickaxeLevel(itemStack);
        PickaxeLevel nextLevel = this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getNextPickaxeLevel(currentLevel);
        Pattern pattern = Pattern.compile("%Enchant-\\d+%");
        for (String string : this.PICKAXE_LORE) {
            string = string
                    .replaceAll("%Blocks%", String.valueOf(getBlocksBroken(itemStack)))
                    .replaceAll("%Blocks_Required%", (nextLevel == null) ? "∞" : String.valueOf(nextLevel.getBlocksRequired()))
                    .replaceAll("%PickaxeLevel%", (currentLevel == null) ? "0" : String.valueOf(currentLevel.getLevel()))
                    .replaceAll("%PickaxeProgress%", this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getProgressBar(itemStack));
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) {
                int id = Integer.parseInt(matcher.group().replaceAll("\\D", ""));
                EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(id);
                if (enchantment != null) {
                    int level = getEnchantLevel(itemStack, id);
                    if (level > 0) string = string.replace(matcher.group(), enchantment.getName() + " " + level);
                    else continue;
                } else continue;
            }
            lore.add(Text.colorize(string));
        }
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
    }

    public long getBlocksBroken(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return 0L;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey(BLOCK_BROKEN_NBT_IDENTIFIER)) return 0L;
        return nbtItem.getLong(BLOCK_BROKEN_NBT_IDENTIFIER);
    }

    public synchronized void addBlocksBrokenToItem(Player player, int amount) {
        if (amount == 0) return;
        NBTItem nbtItem = new NBTItem(player.getItemInHand());
        nbtItem.setLong(BLOCK_BROKEN_NBT_IDENTIFIER,
                nbtItem.hasKey(BLOCK_BROKEN_NBT_IDENTIFIER) ? nbtItem.getLong(BLOCK_BROKEN_NBT_IDENTIFIER) + amount : 0L);
        player.setItemInHand(nbtItem.getItem());
        applyLoreToPickaxe(player.getItemInHand());
    }

    public boolean hasEnchant(Player paramPlayer, int paramInt) {
        ItemStack itemStack = findPickaxe(paramPlayer);
        if (itemStack == null)
            return false;
        return (getEnchantLevel(itemStack, paramInt) != 0);
    }

    public synchronized int getEnchantLevel(ItemStack itemStack, int id) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return 0;
        NBTItem nBTItem = new NBTItem(itemStack);
        return nBTItem.hasKey(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + id) ? nBTItem.getInteger(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + id) : 0;
    }

    public void handleBlockBreak(BlockBreakEvent e, ItemStack itemStack) {
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onBlockBreak(e, hashMap.get(enchantment), ThreadLocalRandom.current().nextDouble(100.0D));
    }

    public void handlePickaxeEquip(Player player, ItemStack itemStack) {
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onEquip(player, itemStack, hashMap.get(enchantment));
    }

    public void handlePickaxeUnequip(Player player, ItemStack itemStack) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onUnequip(player, itemStack, hashMap.get(enchantment));
    }

    public int getInventorySlot(Player player, ItemStack itemStack) {
        for (byte slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (player.getInventory().getItem(slot).equals(itemStack))
                return slot;
        }
        return -1;
    }

    public ItemStack addEnchant(ItemStack itemStack, int id, int levels) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(id);
        if (enchantment == null || itemStack == null) return itemStack;
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (levels > 0) nbtItem.setInteger(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + enchantment.getId(), levels);
        if (!nbtItem.hasKey(PICKAXE_ID_NBT_IDENTIFIER)) nbtItem.setString(PICKAXE_ID_NBT_IDENTIFIER, UUID.randomUUID().toString());
        nbtItem.mergeCustomNBT(itemStack);
        applyLoreToPickaxe(itemStack);
        return itemStack;
    }

    public void removeEnchant(ItemStack itemStack, int id, int levels) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(id);
        if (enchantment == null || itemStack == null || levels == 0) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + id, levels - 1);
        nbtItem.mergeCustomNBT(itemStack);
        applyLoreToPickaxe(itemStack);
    }

    public void setEnchant(ItemStack itemStack, int id, int levels) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(id);
        if (enchantment == null || itemStack == null) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + id, levels);
        nbtItem.mergeCustomNBT(itemStack);
        applyLoreToPickaxe(itemStack);
    }

    public Item getGuiItem(EvilPrisonEnchantment enchantment, EnchantGUI enchantGUI, int currentLevel) {
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(enchantment.getMaterial());
        if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
        itemStackBuilder.name(enchantment.getName());
        itemStackBuilder.lore(translateLore(enchantment, this.ENCHANT_GUI_ITEM_LORE, currentLevel));
        return itemStackBuilder.buildItem().bind(e -> {
            if (!enchantment.canBeBought(enchantGUI.getPickAxe())) return;
            if (e.getClick() == ClickType.LEFT) buyEnchant(enchantment, enchantGUI, currentLevel, 1);
            else if (e.getClick() == ClickType.RIGHT) buyEnchant(enchantment, enchantGUI, currentLevel, 10);
            else if (e.getClick() == ClickType.MIDDLE || e.getClick() == ClickType.SHIFT_RIGHT) buyEnchant(enchantment, enchantGUI, currentLevel, 100);
            else if (e.getClick() == ClickType.DROP) buyEnchant(enchantment, enchantGUI, currentLevel,
                    getMaxEnchantPurchasable(enchantment, currentLevel, this.enchants.getPlugin().getTokens().getApi().getPlayerTokens(enchantGUI.getPlayer())));
            enchantGUI.redraw();
        }, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT, ClickType.DROP).build();
    }

    private int getMaxEnchantPurchasable(EvilPrisonEnchantment enchantment, int currentLevel, long tokens) {
        return (int) Math.floor((float) (enchantment.getIncreaseCost() + 2 * tokens) /
                (2 * currentLevel * enchantment.getIncreaseCost() + 2 * enchantment.getCost() + enchantment.getIncreaseCost()));
    }

    private long getEnchantPrice(EvilPrisonEnchantment enchantment, int currentLevel, int levelsToBuy) {
        return (long) Math.floor((float) (levelsToBuy *
                (2 * currentLevel * enchantment.getIncreaseCost() + enchantment.getCost() +
                        levelsToBuy * enchantment.getIncreaseCost() - enchantment.getIncreaseCost())) / 2);
    }

    public void buyEnchant(EvilPrisonEnchantment enchantment, EnchantGUI enchantGUI, int currentLevel, int levelsToBuy) {
        if (currentLevel >= enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&e&l(!) &eYou have already maxed out this enchantment");
            return;
        }
        if (currentLevel + levelsToBuy > enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&e&l(!) &eThis transaction would exceed the max level for this enchant.\"");
            return;
        }
        long cost = getEnchantPrice(enchantment, currentLevel, levelsToBuy);
        if (!this.enchants.getPlugin().getTokens().getApi().hasEnough(enchantGUI.getPlayer(), cost)) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&c&l(!) &cNot Enough Tokens");
            return;
        }
        this.enchants.getPlugin().getTokens().getApi().removeTokens(enchantGUI.getPlayer(), cost);
        addEnchant(enchantGUI.getPickAxe(), enchantment.getId(), currentLevel + levelsToBuy);
        enchantment.onUnequip(enchantGUI.getPlayer(), enchantGUI.getPickAxe(), currentLevel);
        enchantment.onEquip(enchantGUI.getPlayer(), enchantGUI.getPickAxe(), currentLevel + levelsToBuy);
        enchantGUI.getPlayer().getInventory().setItem(enchantGUI.getPickaxePlayerInventorySlot(), enchantGUI.getPickAxe());
        PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&6&lENCHANTS &8» &fYou purchased &6%amount% %enchant% &flevels for &6⛁%tokens%&f."
                .replace("%amount%", String.valueOf(levelsToBuy))
                .replace("%enchant%", enchantment.getName())
                .replace("%tokens%", String.format("%,d", cost)));
    }

    public void disenchant(EvilPrisonEnchantment enchantment, DisenchantGUI paramDisenchantGUI, int paramInt1, int paramInt2) {
        if (paramInt1 <= 0) {
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_no_level"));
            return;
        }
        long l = 0L;
        enchantment.onUnequip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), paramInt1);
        for (byte b = 0; b < paramInt2; b++, paramInt1--) {
            if (paramInt1 <= 0)
                break;
            long l1 = enchantment.getCostOfLevel(paramInt1);
            removeEnchant(paramDisenchantGUI.getPickAxe(), enchantment.getId(), paramInt1);
            paramDisenchantGUI.getPlayer().getInventory().setItem(paramDisenchantGUI.getPickaxePlayerInventorySlot(), paramDisenchantGUI.getPickAxe());
            l = (long)(l + l1 * this.refundPercentage / 100.0D);
        }
        enchantment.onEquip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), paramInt1);
        this.enchants.getPlugin().getTokens().getApi().addTokens(paramDisenchantGUI.getPlayer(), l, false);
        PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_refunded").replace("%amount%", String.valueOf(paramInt2)).replace("%enchant%", enchantment.getName()));
        PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(l)));
    }

    public void disenchantMax(EvilPrisonEnchantment enchantment, DisenchantGUI paramDisenchantGUI, int paramInt) {
        if (paramInt <= 0) {
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_no_level"));
            return;
        }
        if (this.lockedPlayers.contains(paramDisenchantGUI.getPlayer().getUniqueId())) {
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("transaction_in_progress"));
            return;
        }
        this.lockedPlayers.add(paramDisenchantGUI.getPlayer().getUniqueId());
        Schedulers.async().run(() -> {
            int i = paramInt;
            int j = i;
            long l = 0L;
            while (paramDisenchantGUI.getPlayer().isOnline() && i > 0) {
                long l1 = enchantment.getCostOfLevel(i);
                l = (long)(l + l1 * this.refundPercentage / 100.0D);
                i--;
            }
            if (!paramDisenchantGUI.getPlayer().isOnline()) {
                this.lockedPlayers.remove(paramDisenchantGUI.getPlayer().getUniqueId());
                return;
            }
            this.lockedPlayers.remove(paramDisenchantGUI.getPlayer().getUniqueId());
            Schedulers.sync().run(() -> {
                enchantment.onUnequip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), paramInt);
                this.setEnchant(paramDisenchantGUI.getPickAxe(), enchantment.getId(), j);
                paramDisenchantGUI.getPlayer().getInventory().setItem(paramDisenchantGUI.getPickaxePlayerInventorySlot(), paramDisenchantGUI.getPickAxe());
                enchantment.onEquip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), j);
                paramDisenchantGUI.redraw();
            });
            this.enchants.getPlugin().getTokens().getApi().addTokens(paramDisenchantGUI.getPlayer(), l, false);
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_refunded").replace("%amount%", String.valueOf(j)).replace("%enchant%", enchantment.getName()));
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(l)));
        });
    }

    public me.lucko.helper.menu.Item getRefundGuiItem(EvilPrisonEnchantment enchantment, DisenchantGUI paramDisenchantGUI, int paramInt) {
        Material material = enchantment.isRefundEnabled() ? enchantment.getMaterial() : Material.BARRIER;
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(material);
        if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
        itemStackBuilder.name(enchantment.isRefundEnabled() ? enchantment.getName() : this.enchants.getMessage("enchant_cant_disenchant"));
        itemStackBuilder.lore(enchantment.isRefundEnabled() ? translateLore(enchantment, this.DISENCHANT_GUI_ITEM_LORE, paramInt) : new ArrayList<>());
        return enchantment.isRefundEnabled() ? itemStackBuilder.buildItem().bind(paramInventoryClickEvent -> {
            if (paramInventoryClickEvent.getClick() == ClickType.MIDDLE || paramInventoryClickEvent.getClick() == ClickType.SHIFT_RIGHT) {
                disenchant(enchantment, paramDisenchantGUI, paramInt, 100);
                paramDisenchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.LEFT) {
                disenchant(enchantment, paramDisenchantGUI, paramInt, 1);
                paramDisenchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.RIGHT) {
                disenchant(enchantment, paramDisenchantGUI, paramInt, 10);
                paramDisenchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.DROP) {
                disenchantMax(enchantment, paramDisenchantGUI, paramInt);
            }
        }, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.LEFT, ClickType.RIGHT, ClickType.DROP).build() : itemStackBuilder.buildConsumer(paramInventoryClickEvent -> paramInventoryClickEvent.getWhoClicked().sendMessage(this.enchants.getMessage("enchant_cant_disenchant")));
    }

    public List<String> translateLore(EvilPrisonEnchantment enchantment, List<String> paramList, int paramInt) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : paramList) {
            if (str.contains("%description%")) {
                arrayList.addAll(enchantment.getDescription());
                continue;
            }
            arrayList.add(str
                    .replace("%refund%", String.format("%,d", getRefundForLevel(enchantment, paramInt))).replace("%cost%", String.format("%,d", enchantment.getCost() + enchantment.getIncreaseCost() * paramInt)).replace("%max_level%", (enchantment.getMaxLevel() == Integer.MAX_VALUE) ? "Unlimited" : String.format("%,d", enchantment.getMaxLevel())).replace("%current_level%", String.format("%,d", paramInt)).replace("%pickaxe_level%", String.format("%,d", enchantment.getRequiredPickaxeLevel())));
        }
        return arrayList;
    }

    private long getRefundForLevel(EvilPrisonEnchantment enchantment, int paramInt) {
        return (long)((enchantment.getCost() + enchantment.getIncreaseCost() * (paramInt - 1)) * this.refundPercentage / 100.0D);
    }

    public long getPickaxeValue(ItemStack paramItemStack) {
        long l = 0L;
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(paramItemStack);
        for (EvilPrisonEnchantment ultraPrisonEnchantment : hashMap.keySet()) {
            for (byte b = 1; b <= hashMap.get(ultraPrisonEnchantment); b++)
                l += ultraPrisonEnchantment.getCostOfLevel(b);
        }
        return l;
    }

    public void reload() {
        this.ENCHANT_GUI_ITEM_LORE = this.enchants.getConfig().getStringList("enchant_menu.item.lore");
        this.DISENCHANT_GUI_ITEM_LORE = this.enchants.getConfig().getStringList("disenchant_menu.item.lore");
        this.PICKAXE_LORE = this.enchants.getConfig().getStringList("Pickaxe.lore");
        this.openEnchantMenuOnRightClickBlock = this.enchants.getConfig().getBoolean("open-menu-on-right-click-block");
        this.allowEnchantsOutside = this.enchants.getConfig().getBoolean("allow-enchants-outside-mine-regions");
        this.refundPercentage = this.enchants.getConfig().getDouble("refund-percentage");
        this.firstJoinPickaxeEnabled = this.enchants.getConfig().getBoolean("first-join-pickaxe.enabled");
        this.firstJoinPickaxeMaterial = Material.getMaterial(this.enchants.getConfig().getString("first-join-pickaxe.material"));
        this.firstJoinPickaxeEnchants = this.enchants.getConfig().getStringList("first-join-pickaxe.enchants");
        this.firstJoinPickaxeName = this.enchants.getConfig().getString("first-join-pickaxe.name");
    }

    public void givePickaxe(Player paramPlayer, String paramString1, String paramString2, CommandSender paramCommandSender) {
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(Material.DIAMOND_PICKAXE);
        if (paramString2 != null)
            itemStackBuilder.name(paramString2);
        ItemStack itemStack = itemStackBuilder.build();
        String[] arrayOfString = paramString1.split(",");
        for (String str : arrayOfString) {
            String[] arrayOfString1 = str.split("=");
            try {
                EvilPrisonEnchantment ultraPrisonEnchantment = EvilPrisonEnchantment.getEnchantByName(arrayOfString1[0]);
                if (ultraPrisonEnchantment == null)
                    ultraPrisonEnchantment = EvilPrisonEnchantment.getEnchantById(Integer.parseInt(arrayOfString1[0]));
                if (ultraPrisonEnchantment != null) {
                    int i = Integer.parseInt(arrayOfString1[1]);
                    itemStack = addEnchant(itemStack, ultraPrisonEnchantment.getId(), i);
                }
            } catch (Exception ignored) {}
        }
        applyLoreToPickaxe(itemStack);
        if (paramPlayer == null && paramCommandSender instanceof Player)
            paramPlayer = (Player)paramCommandSender;
        if (paramPlayer != null) {
            if (paramPlayer.getInventory().firstEmpty() == -1) {
                PlayerUtils.sendMessage(paramCommandSender, this.enchants.getMessage("pickaxe_inventory_full").replace("%player%", paramPlayer.getName()));
                return;
            }
            paramPlayer.getInventory().addItem(itemStack);
            PlayerUtils.sendMessage(paramCommandSender, this.enchants.getMessage("pickaxe_given").replace("%player%", paramPlayer.getName()));
            PlayerUtils.sendMessage(paramPlayer, this.enchants.getMessage("pickaxe_received").replace("%sender%", paramCommandSender.getName()));
        }
    }

    private ItemStack addUniqueTagToPickaxe(ItemStack paramItemStack) {
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (!nBTItem.hasKey("pickaxe-id"))
            nBTItem.setString("pickaxe-id", UUID.randomUUID().toString());
        paramItemStack = nBTItem.getItem();
        return paramItemStack;
    }

    public ItemStack createFirstJoinPickaxe(Player paramPlayer) {
        ItemStack itemStack = ItemStackBuilder.of(this.firstJoinPickaxeMaterial).name(this.firstJoinPickaxeName.replace("%player%", paramPlayer.getName())).build();
        for (String str : this.firstJoinPickaxeEnchants) {
            try {
                String[] arrayOfString = str.split(" ");
                EvilPrisonEnchantment ultraPrisonEnchantment = EvilPrisonEnchantment.getEnchantByName(arrayOfString[0]);
                int i = Integer.parseInt(arrayOfString[1]);
                itemStack = addEnchant(itemStack, ultraPrisonEnchantment.getId(), i);
            } catch (Exception ignored) {}
        }
        applyLoreToPickaxe(itemStack);
        return itemStack;
    }

    public boolean hasEnchants(ItemStack paramItemStack) {
        return !getPlayerEnchants(paramItemStack).isEmpty();
    }
}
