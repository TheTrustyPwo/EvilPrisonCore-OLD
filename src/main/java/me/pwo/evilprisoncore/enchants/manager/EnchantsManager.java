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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class EnchantsManager {
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

    public boolean isOpenEnchantMenuOnRightClickBlock() {
        return this.openEnchantMenuOnRightClickBlock;
    }

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
            int i = getEnchantLevel(itemStack, enchantment.getId());
            if (i == 0)
                continue;
            hashMap.put(enchantment, i);
        }
        return hashMap;
    }

    public ItemStack findPickaxe(Player player) {
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null)
                continue;
            if (this.enchants.getPlugin().isPickaxeSupported(itemStack.getType()))
                return itemStack;
        }
        return null;
    }

    public void updatePickaxe(ItemStack itemStack) {
        if (itemStack == null || !this.enchants.getPlugin().isPickaxeSupported(itemStack.getType()))
            return;
        applyLoreToPickaxe(itemStack);
    }

    private void applyLoreToPickaxe(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        ArrayList<String> arrayList = new ArrayList();
        boolean bool = this.enchants.getPlugin().isModuleEnabled("Pickaxe Levels");
        PickaxeLevel pickaxeLevel1 = null;
        PickaxeLevel pickaxeLevel2 = null;
        if (bool) {
            pickaxeLevel1 = this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getPickaxeLevel(itemStack);
            pickaxeLevel2 = this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getNextPickaxeLevel(pickaxeLevel1);
        }
        Pattern pattern = Pattern.compile("%Enchant-\\d+%");
        for (String str : this.PICKAXE_LORE) {
            str = str.replace("%Blocks%", String.valueOf(getBlocksBroken(itemStack)));
            if (bool) {
                str = str.replace("%Blocks_Required%", (pickaxeLevel2 == null) ? "∞" : String.valueOf(pickaxeLevel2.getBlocksRequired()));
                        str = str.replace("%PickaxeLevel%", (pickaxeLevel1 == null) ? "0" : String.valueOf(pickaxeLevel1.getLevel()));
                str = str.replace("%PickaxeProgress%", this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getProgressBar(itemStack));
            }
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                int i = Integer.parseInt(matcher.group().replaceAll("\\D", ""));
                EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(i);
                if (enchantment != null) {
                    int j = getEnchantLevel(itemStack, i);
                    if (j > 0) {
                        str = str.replace(matcher.group(), enchantment.getName() + " " + j);
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            arrayList.add(Text.colorize(str));
        }
        itemMeta.setLore(arrayList);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
    }

    public long getBlocksBroken(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return 0L;
        NBTItem nBTItem = new NBTItem(itemStack);
        if (!nBTItem.hasKey("blocks-broken"))
            return 0L;
        return nBTItem.getLong("blocks-broken");
    }

    public synchronized void addBlocksBrokenToItem(Player player, int paramInt) {
        if (paramInt == 0)
            return;
        NBTItem nBTItem = new NBTItem(player.getItemInHand());
        try {
            int i = nBTItem.getInteger("blocks-broken");
            if (i > 0) {
                paramInt += i;
                nBTItem.removeKey("blocks-broken");
            }
        } catch (Exception ignored) {}
        if (!nBTItem.hasKey("blocks-broken"))
            nBTItem.setLong("blocks-broken", 0L);
        nBTItem.setLong("blocks-broken", nBTItem.getLong("blocks-broken") + paramInt);
        player.setItemInHand(nBTItem.getItem());
        applyLoreToPickaxe(player.getItemInHand());
    }

    public boolean hasEnchant(Player paramPlayer, int paramInt) {
        ItemStack itemStack = findPickaxe(paramPlayer);
        if (itemStack == null)
            return false;
        return (getEnchantLevel(itemStack, paramInt) != 0);
    }

    public synchronized int getEnchantLevel(ItemStack paramItemStack, int paramInt) {
        if (paramItemStack == null || paramItemStack.getType() == Material.AIR)
            return 0;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (!nBTItem.hasKey("ultra-prison-ench-" + paramInt))
            return 0;
        return nBTItem.getInteger("ultra-prison-ench-" + paramInt);
    }

    public void handleBlockBreak(BlockBreakEvent e, ItemStack itemStack) {
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(itemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onBlockBreak(e, hashMap.get(enchantment));
    }

    public void handlePickaxeEquip(Player paramPlayer, ItemStack paramItemStack) {
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(paramItemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onEquip(paramPlayer, paramItemStack, hashMap.get(enchantment));
    }

    public void handlePickaxeUnequip(Player paramPlayer, ItemStack paramItemStack) {
        paramPlayer.getActivePotionEffects().forEach(paramPotionEffect -> paramPlayer.removePotionEffect(paramPotionEffect.getType()));
        HashMap<EvilPrisonEnchantment, Integer> hashMap = getPlayerEnchants(paramItemStack);
        for (EvilPrisonEnchantment enchantment : hashMap.keySet())
            enchantment.onUnequip(paramPlayer, paramItemStack, hashMap.get(enchantment));
    }

    public int getInventorySlot(Player paramPlayer, ItemStack paramItemStack) {
        for (byte b = 0; b < paramPlayer.getInventory().getSize(); b++) {
            ItemStack itemStack = paramPlayer.getInventory().getItem(b);
            if (itemStack != null)
                if (itemStack.equals(paramItemStack))
                    return b;
        }
        return -1;
    }

    public ItemStack addEnchant(Player paramPlayer, ItemStack paramItemStack, int paramInt1, int paramInt2) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(paramInt1);
        if (enchantment == null || paramItemStack == null)
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack, true);
        if (paramInt2 > 0)
            nBTItem.setInteger("ultra-prison-ench-" + enchantment.getId(), paramInt2);
        if (!nBTItem.hasKey("pickaxe-id"))
            nBTItem.setString("pickaxe-id", UUID.randomUUID().toString());
        nBTItem.mergeCustomNBT(paramItemStack);
        applyLoreToPickaxe(paramItemStack);
        return paramItemStack;
    }

    public ItemStack addEnchant(ItemStack paramItemStack, int paramInt1, int paramInt2) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(paramInt1);
        if (enchantment == null || paramItemStack == null)
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (paramInt2 > 0)
            nBTItem.setInteger("ultra-prison-ench-" + enchantment.getId(), paramInt2);
        if (!nBTItem.hasKey("pickaxe-id"))
            nBTItem.setString("pickaxe-id", UUID.randomUUID().toString());
        paramItemStack = nBTItem.getItem();
        applyLoreToPickaxe(paramItemStack);
        return paramItemStack;
    }

    public void addEnchant(Player player, ItemStack itemStack, EvilPrisonEnchantment enchantment, int paramInt) {
        addEnchant(player, itemStack, enchantment.getId(), paramInt);
    }

    public boolean removeEnchant(Player paramPlayer, int paramInt) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(paramInt);
        if (enchantment == null || paramPlayer.getItemInHand() == null)
            return false;
        ItemStack itemStack = paramPlayer.getItemInHand();
        NBTItem nBTItem = new NBTItem(itemStack);
        nBTItem.removeKey("ultra-prison-ench-" + paramInt);
        paramPlayer.setItemInHand(nBTItem.getItem());
        applyLoreToPickaxe(paramPlayer.getItemInHand());
        return true;
    }

    public ItemStack removeEnchant(ItemStack paramItemStack, Player paramPlayer, int paramInt1, int paramInt2) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(paramInt1);
        if (enchantment == null || paramItemStack == null || paramInt2 == 0)
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        nBTItem.setInteger("ultra-prison-ench-" + paramInt1, paramInt2 - 1);
        nBTItem.mergeCustomNBT(paramItemStack);
        applyLoreToPickaxe(paramItemStack);
        return paramItemStack;
    }

    public ItemStack setEnchant(ItemStack paramItemStack, Player paramPlayer, int paramInt1, int paramInt2) {
        EvilPrisonEnchantment enchantment = EvilPrisonEnchantment.getEnchantById(paramInt1);
        if (enchantment == null || paramItemStack == null)
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        nBTItem.setInteger("ultra-prison-ench-" + paramInt1, paramInt2);
        nBTItem.mergeCustomNBT(paramItemStack);
        applyLoreToPickaxe(paramItemStack);
        return paramItemStack;
    }

    public boolean buyEnchant(EvilPrisonEnchantment enchantment, EnchantGUI paramEnchantGUI, int paramInt1, int paramInt2) {
        if (paramInt1 >= enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_max_level"));
            return false;
        }
        if (paramInt1 + paramInt2 > enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_max_level_exceed"));
            return false;
        }
        long l1 = 0L;
        for (byte b = 0; b < paramInt2; b++)
            l1 += enchantment.getCostOfLevel(paramInt1 + b + 1);
        if (!this.enchants.getPlugin().getTokens().getApi().hasEnough(paramEnchantGUI.getPlayer(), l1)) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("not_enough_tokens"));
            return false;
        }
        this.enchants.getPlugin().getTokens().getApi().removeTokens(paramEnchantGUI.getPlayer(), l1);
        addEnchant(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), enchantment.getId(), paramInt1 + paramInt2);
        enchantment.onUnequip(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), paramInt1);
        enchantment.onEquip(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), paramInt1 + paramInt2);
        paramEnchantGUI.getPlayer().getInventory().setItem(paramEnchantGUI.getPickaxePlayerInventorySlot(), paramEnchantGUI.getPickAxe());
        if (paramInt2 == 1) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_bought").replace("%tokens%", String.valueOf(l1)));
        } else {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_bought_multiple")
                    .replace("%amount%", String.valueOf(paramInt2))
                    .replace("%enchant%", enchantment.getName())
                    .replace("%tokens%", String.format("%,d", l1)));
        }
        return true;
    }

    public boolean disenchant(EvilPrisonEnchantment enchantment, DisenchantGUI paramDisenchantGUI, int paramInt1, int paramInt2) {
        if (paramInt1 <= 0) {
            PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_no_level"));
            return false;
        }
        long l = 0L;
        enchantment.onUnequip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), paramInt1);
        for (byte b = 0; b < paramInt2; b++, paramInt1--) {
            if (paramInt1 <= 0)
                break;
            long l1 = enchantment.getCostOfLevel(paramInt1);
            removeEnchant(paramDisenchantGUI.getPickAxe(), paramDisenchantGUI.getPlayer(), enchantment.getId(), paramInt1);
            paramDisenchantGUI.getPlayer().getInventory().setItem(paramDisenchantGUI.getPickaxePlayerInventorySlot(), paramDisenchantGUI.getPickAxe());
            l = (long)(l + l1 * this.refundPercentage / 100.0D);
        }
        enchantment.onEquip(paramDisenchantGUI.getPlayer(), paramDisenchantGUI.getPickAxe(), paramInt1);
        this.enchants.getPlugin().getTokens().getApi().addTokens(paramDisenchantGUI.getPlayer(), l, false);
        PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_refunded").replace("%amount%", String.valueOf(paramInt2)).replace("%enchant%", enchantment.getName()));
        PlayerUtils.sendMessage(paramDisenchantGUI.getPlayer(), this.enchants.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(l)));
        return true;
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
                this.setEnchant(paramDisenchantGUI.getPickAxe(), paramDisenchantGUI.getPlayer(), enchantment.getId(), j);
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
        itemStackBuilder.lore(enchantment.isRefundEnabled() ? translateLore(enchantment, this.DISENCHANT_GUI_ITEM_LORE, paramInt) : new ArrayList());
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
        }, new ClickType[] { ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.LEFT, ClickType.RIGHT, ClickType.DROP }).build() : itemStackBuilder.buildConsumer(paramInventoryClickEvent -> paramInventoryClickEvent.getWhoClicked().sendMessage(this.enchants.getMessage("enchant_cant_disenchant")));
    }

    public Item getGuiItem(EvilPrisonEnchantment enchantment, EnchantGUI paramEnchantGUI, int paramInt) {
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(enchantment.getMaterial());
        if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
        itemStackBuilder.name(enchantment.getName());
        itemStackBuilder.lore(translateLore(enchantment, this.ENCHANT_GUI_ITEM_LORE, paramInt));
        return itemStackBuilder.buildItem().bind(paramInventoryClickEvent -> {
            if (!enchantment.canBeBought(paramEnchantGUI.getPickAxe()))
                return;
            if (paramInventoryClickEvent.getClick() == ClickType.MIDDLE || paramInventoryClickEvent.getClick() == ClickType.SHIFT_RIGHT) {
                buyEnchant(enchantment, paramEnchantGUI, paramInt, 100);
                paramEnchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.LEFT) {
                buyEnchant(enchantment, paramEnchantGUI, paramInt, 1);
                paramEnchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.RIGHT) {
                buyEnchant(enchantment, paramEnchantGUI, paramInt, 10);
                paramEnchantGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.DROP) {
                buyMaxEnchant(enchantment, paramEnchantGUI, paramInt);
            }
        }, new ClickType[] { ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT, ClickType.DROP }).build();
    }

    private void buyMaxEnchant(EvilPrisonEnchantment enchantment, EnchantGUI paramEnchantGUI, int paramInt) {
        if (paramInt >= enchantment.getMaxLevel()) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_max_level"));
            return;
        }
        if (this.lockedPlayers.contains(paramEnchantGUI.getPlayer().getUniqueId())) {
            PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("transaction_in_progress"));
            return;
        }
        this.lockedPlayers.add(paramEnchantGUI.getPlayer().getUniqueId());
        Schedulers.async().run(() -> {
            byte b1 = 0;
            long l = 0L;
            while (paramEnchantGUI.getPlayer().isOnline() && paramInt + b1 + 1 <= enchantment.getMaxLevel() && this.enchants.getPlugin().getTokens().getApi().hasEnough(paramEnchantGUI.getPlayer(), l + enchantment.getCostOfLevel(paramInt + b1 + 1)))
                l += enchantment.getCostOfLevel(paramInt + ++b1 + 1);
            if (!paramEnchantGUI.getPlayer().isOnline()) {
                this.lockedPlayers.remove(paramEnchantGUI.getPlayer().getUniqueId());
                return;
            }
            if (b1 == 0) {
                this.lockedPlayers.remove(paramEnchantGUI.getPlayer().getUniqueId());
                PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("not_enough_tokens"));
                return;
            }
            this.enchants.getPlugin().getTokens().getApi().removeTokens(paramEnchantGUI.getPlayer(), l);
            this.lockedPlayers.remove(paramEnchantGUI.getPlayer().getUniqueId());
            byte finalB = b1;
            Schedulers.sync().run(() -> {
                enchantment.onUnequip(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), paramInt);
                this.addEnchant(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), enchantment.getId(), paramInt + finalB);
                enchantment.onEquip(paramEnchantGUI.getPlayer(), paramEnchantGUI.getPickAxe(), paramInt + finalB);
                paramEnchantGUI.getPlayer().getInventory().setItem(paramEnchantGUI.getPickaxePlayerInventorySlot(), paramEnchantGUI.getPickAxe());
                paramEnchantGUI.redraw();
            });
            if (b1 == 1) {
                PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_bought").replace("%tokens%", String.valueOf(l)));
            } else {
                PlayerUtils.sendMessage(paramEnchantGUI.getPlayer(), this.enchants.getMessage("enchant_bought_multiple").replace("%amount%", String.valueOf(b1)).replace("%enchant%", enchantment.getName()).replace("%tokens%", String.format("%,d", l)));
            }
        });
    }

    private List<String> translateLore(EvilPrisonEnchantment enchantment, List<String> paramList, int paramInt) {
        ArrayList<String> arrayList = new ArrayList();
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
