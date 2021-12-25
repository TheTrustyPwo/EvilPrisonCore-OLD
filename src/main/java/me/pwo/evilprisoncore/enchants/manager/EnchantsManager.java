package me.pwo.evilprisoncore.enchants.manager;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
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

@SuppressWarnings("deprecation")
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

    public HashMap<EvilEnchant, Integer> getPlayerEnchants(ItemStack itemStack) {
        HashMap<EvilEnchant, Integer> hashMap = new HashMap<>();
        for (EvilEnchant enchantment : EvilEnchant.all()) {
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
                EvilEnchant enchantment = EvilEnchant.getEnchantById(id);
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
        HashMap<EvilEnchant, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilEnchant enchantment : hashMap.keySet())
            enchantment.onBlockBreak(e, hashMap.get(enchantment), ThreadLocalRandom.current().nextDouble(100.0D));
    }

    public void handlePickaxeEquip(Player player, ItemStack itemStack) {
        HashMap<EvilEnchant, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilEnchant enchantment : hashMap.keySet())
            enchantment.onEquip(player, itemStack, hashMap.get(enchantment));
    }

    public void handlePickaxeUnequip(Player player, ItemStack itemStack) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        HashMap<EvilEnchant, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilEnchant enchantment : hashMap.keySet())
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
        EvilEnchant enchantment = EvilEnchant.getEnchantById(id);
        if (enchantment == null || itemStack == null) return itemStack;
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (levels >= 0) nbtItem.setInteger(ENCHANT_LEVEL_NBT_IDENTIFIER_PREFIX + enchantment.getId(), levels);
        if (!nbtItem.hasKey(PICKAXE_ID_NBT_IDENTIFIER)) nbtItem.setString(PICKAXE_ID_NBT_IDENTIFIER, UUID.randomUUID().toString());
        nbtItem.mergeCustomNBT(itemStack);
        applyLoreToPickaxe(itemStack);
        return itemStack;
    }

    public Item getGuiItem(EvilEnchant enchantment, EnchantGUI enchantGUI, int currentLevel) {
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(enchantment.getMaterial());
        if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
        itemStackBuilder.name(enchantment.getName());
        itemStackBuilder.lore(translateLore(enchantment, this.ENCHANT_GUI_ITEM_LORE, currentLevel));
        return itemStackBuilder.buildItem().bind(e -> {
            if (!enchantment.canBeBought(enchantGUI.getPickaxe())) return;
            if (e.getClick() == ClickType.LEFT) buyEnchant(enchantment, enchantGUI, currentLevel, 1);
            else if (e.getClick() == ClickType.RIGHT) buyEnchant(enchantment, enchantGUI, currentLevel, 10);
            else if (e.getClick() == ClickType.MIDDLE || e.getClick() == ClickType.SHIFT_RIGHT) buyEnchant(enchantment, enchantGUI, currentLevel, 100);
            else if (e.getClick() == ClickType.DROP) buyMaxEnchant(enchantment, enchantGUI, currentLevel);
            enchantGUI.redraw();
        }, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT, ClickType.DROP).build();
    }

    private int getMaxEnchantPurchasable(EvilEnchant enchantment, int currentLevel, long tokens) {
        return (int) Math.floor((float) (enchantment.getIncreaseCost() + 2 * tokens) /
                (2 * currentLevel * enchantment.getIncreaseCost() + 2 * enchantment.getCost() + enchantment.getIncreaseCost()));
    }

    private long getEnchantPrice(EvilEnchant enchantment, int currentLevel, int levelsToBuy) {
        return (long) Math.floor((float) (levelsToBuy *
                (2 * currentLevel * enchantment.getIncreaseCost() + enchantment.getCost() +
                        levelsToBuy * enchantment.getIncreaseCost() - enchantment.getIncreaseCost())) / 2);
    }

    public void buyEnchant(EvilEnchant enchantment, EnchantGUI enchantGUI, int currentLevel, int levelsToBuy) {
        if (currentLevel >= enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&e&l(!) &eYou have already maxed out this enchantment");
            return;
        }
        levelsToBuy = Math.min(levelsToBuy, enchantment.getMaxLevel() - currentLevel);
        long cost = getEnchantPrice(enchantment, currentLevel, levelsToBuy);
        if (!this.enchants.getPlugin().getTokens().getApi().hasEnough(enchantGUI.getPlayer(), cost)) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&c&l(!) &cNot Enough Tokens");
            return;
        }
        this.enchants.getPlugin().getTokens().getApi().removeTokens(enchantGUI.getPlayer(), cost);
        addEnchant(enchantGUI.getPickaxe(), enchantment.getId(), currentLevel + levelsToBuy);
        enchantment.onUnequip(enchantGUI.getPlayer(), enchantGUI.getPickaxe(), currentLevel);
        enchantment.onEquip(enchantGUI.getPlayer(), enchantGUI.getPickaxe(), currentLevel + levelsToBuy);
        enchantGUI.getPlayer().getInventory().setItem(enchantGUI.getPickaxePlayerInventorySlot(), enchantGUI.getPickaxe());
        PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&eYou purchased &6%amount% %enchant% &elevels for &6⛁%tokens%&e."
                .replace("%amount%", String.valueOf(levelsToBuy))
                .replace("%enchant%", enchantment.getName())
                .replace("%tokens%", String.format("%,d", cost)), true);
    }

    public void buyMaxEnchant(EvilEnchant enchantment, EnchantGUI enchantGUI, int currentLevel) {
        if (currentLevel >= enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&c&l(!) &cYou have already maxed out this enchantment");
            return;
        }
        int levelsToBuy = Math.min(getMaxEnchantPurchasable(enchantment, currentLevel, this.enchants.getPlugin().getTokens().getApi().getPlayerTokens(enchantGUI.getPlayer())), enchantment.getMaxLevel());
        if (levelsToBuy == 0) {
            PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&c&l(!) &cNot Enough Tokens");
            return;
        }
        long cost = getEnchantPrice(enchantment, currentLevel, levelsToBuy);
        this.enchants.getPlugin().getTokens().getApi().removeTokens(enchantGUI.getPlayer(), cost);
        addEnchant(enchantGUI.getPickaxe(), enchantment.getId(), currentLevel + levelsToBuy);
        enchantment.onUnequip(enchantGUI.getPlayer(), enchantGUI.getPickaxe(), currentLevel);
        enchantment.onEquip(enchantGUI.getPlayer(), enchantGUI.getPickaxe(), currentLevel + levelsToBuy);
        enchantGUI.getPlayer().getInventory().setItem(enchantGUI.getPickaxePlayerInventorySlot(), enchantGUI.getPickaxe());
        PlayerUtils.sendMessage(enchantGUI.getPlayer(), "&eYou purchased &6%amount% %enchant% &elevels for &6⛁%tokens%&e."
                .replace("%amount%", String.valueOf(levelsToBuy))
                .replace("%enchant%", enchantment.getName())
                .replace("%tokens%", String.format("%,d", cost)), true);
    }

    public void disenchant(EvilEnchant enchantment, DisenchantGUI disenchantGUI, int currentLevel, int levelsToRemove) {
        if (currentLevel <= 0) {
            PlayerUtils.sendMessage(disenchantGUI.getPlayer(), "&c&l(!) &cYou do not have this enchant!");
            return;
        }
        enchantment.onUnequip(disenchantGUI.getPlayer(), disenchantGUI.getPickaxe(), currentLevel);
        levelsToRemove = Math.min(currentLevel, levelsToRemove);
        long amount = (long) (getEnchantPrice(enchantment, currentLevel - levelsToRemove, levelsToRemove) * (this.refundPercentage / 100.0D));
        addEnchant(disenchantGUI.getPickaxe(), enchantment.getId(), currentLevel - levelsToRemove);
        disenchantGUI.getPlayer().getInventory().setItem(disenchantGUI.getPickaxePlayerInventorySlot(), disenchantGUI.getPickaxe());
        enchantment.onEquip(disenchantGUI.getPlayer(), disenchantGUI.getPickaxe(), currentLevel - levelsToRemove);
        disenchantGUI.getPlayer().getInventory().setItem(disenchantGUI.getPickaxePlayerInventorySlot(), disenchantGUI.getPickaxe());
        this.enchants.getPlugin().getTokens().getApi().addTokens(disenchantGUI.getPlayer(), amount, false);
        PlayerUtils.sendMessage(disenchantGUI.getPlayer(), "&eYou refunded &6%amount% %enchant% &elevels for &6⛁%tokens%&e."
                .replace("%amount%", String.valueOf(levelsToRemove))
                .replace("%enchant%", enchantment.getName())
                .replace("%tokens%", String.format("%,d", amount)), true);
    }

    public Item getRefundGuiItem(EvilEnchant enchantment, DisenchantGUI disenchantGUI, int currentLevel) {
        Material material = enchantment.isRefundEnabled() ? enchantment.getMaterial() : Material.BARRIER;
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(material);
        if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
        itemStackBuilder.name(enchantment.isRefundEnabled() ? enchantment.getName() : "&c&l(!) &cThis enchant can't be disenchanted.");
        itemStackBuilder.lore(enchantment.isRefundEnabled() ? translateLore(enchantment, this.DISENCHANT_GUI_ITEM_LORE, currentLevel) : new ArrayList<>());
        return enchantment.isRefundEnabled() ? itemStackBuilder.buildItem().bind(e -> {
            if (e.getClick() == ClickType.LEFT) disenchant(enchantment, disenchantGUI, currentLevel, 1);
            else if (e.getClick() == ClickType.RIGHT) disenchant(enchantment, disenchantGUI, currentLevel, 10);
            else if (e.getClick() == ClickType.MIDDLE || e.getClick() == ClickType.SHIFT_RIGHT) disenchant(enchantment, disenchantGUI, currentLevel, 100);
            else if (e.getClick() == ClickType.DROP) disenchant(enchantment, disenchantGUI, currentLevel, currentLevel);
            disenchantGUI.redraw();
        },  ClickType.LEFT, ClickType.RIGHT, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.DROP).build() : itemStackBuilder.buildConsumer(paramInventoryClickEvent -> paramInventoryClickEvent.getWhoClicked().sendMessage(this.enchants.getMessage("enchant_cant_disenchant")));
    }

    public List<String> translateLore(EvilEnchant enchantment, List<String> paramList, int paramInt) {
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

    private long getRefundForLevel(EvilEnchant enchantment, int paramInt) {
        return (long)((enchantment.getCost() + enchantment.getIncreaseCost() * (paramInt - 1)) * this.refundPercentage / 100.0D);
    }

    public long getPickaxeValue(ItemStack paramItemStack) {
        long l = 0L;
        HashMap<EvilEnchant, Integer> hashMap = getPlayerEnchants(paramItemStack);
        for (EvilEnchant ultraPrisonEnchantment : hashMap.keySet()) {
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
                EvilEnchant ultraPrisonEnchantment = EvilEnchant.getEnchantByName(arrayOfString1[0]);
                if (ultraPrisonEnchantment == null)
                    ultraPrisonEnchantment = EvilEnchant.getEnchantById(Integer.parseInt(arrayOfString1[0]));
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
                EvilEnchant ultraPrisonEnchantment = EvilEnchant.getEnchantByName(arrayOfString[0]);
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
