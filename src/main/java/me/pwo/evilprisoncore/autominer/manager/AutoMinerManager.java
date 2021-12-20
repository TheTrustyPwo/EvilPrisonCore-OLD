package me.pwo.evilprisoncore.autominer.manager;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.autominer.gui.AutoMinerGUI;
import me.pwo.evilprisoncore.autominer.upgrades.AutoMinerUpgrade;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AutoMinerManager {
    private final AutoMiner autoMiner;
    private static AutoMinerManager instance;
    private static final List<String> UPGRADE_GUI_ITEM_LORE = Arrays.asList(
            "&7%description%",
            " ",
            "&6&l* &ePrice: &f%cost%",
            "&6&l* &eMax Level: &f%max_level%",
            "&6&l* &eCurrent Level: &f%current_level%",
            " ",
            "&7&o(( &f&oLeft-Click&7&o to add 1 level ))",
            "&7&o(( &f&oRight-Click&7&o to add 10 levels ))",
            "&7&o(( &f&oMiddle-Click&7&o to add 100 levels ))",
            "&7&o(( &f&oPress 'Q'&7&o to buy as much as you can afford ))");

    public static AutoMinerManager getInstance() { return instance; }

    public AutoMinerManager(AutoMiner autoMiner) {
        this.autoMiner = autoMiner;
        instance = this;
    }

    public synchronized int getUpgradeLevel(ItemStack paramItemStack, int paramInt) {
        if (paramItemStack == null || paramItemStack.getType() == Material.AIR)
            return 0;
        NBTItem nBTItem = new NBTItem(paramItemStack);
        if (!nBTItem.hasKey("EvilPrison-Autominer-Upgrade-" + paramInt))
            return 0;
        return nBTItem.getInteger("EvilPrison-Autominer-Upgrade-" + paramInt);
    }

    public boolean buyUpgrade(AutoMinerUpgrade upgrade, AutoMinerGUI autoMinerGUI, int currentLevel, int upgradeLevel) {
        if (currentLevel >= upgrade.getMaxLevel()) {
            PlayerUtils.sendMessage(autoMinerGUI.getPlayer(), "&e&l(!) &eYou have already maxed out this upgrade!");
            return false;
        }
        if (currentLevel + upgradeLevel > upgrade.getMaxLevel()) {
            PlayerUtils.sendMessage(autoMinerGUI.getPlayer(), "&e&l(!) &eThis transaction would exceed the max level for this upgrade!");
            return false;
        }
        long cost = 0L;
        for (byte b = 0; b < upgradeLevel; b++)
            cost += upgrade.getCostOfLevel(currentLevel + b + 1);
        if (!this.autoMiner.getPlugin().getTokens().getApi().hasEnough(autoMinerGUI.getPlayer(), cost)) {
            PlayerUtils.sendMessage(autoMinerGUI.getPlayer(), "&cYou do not have enough tokens!");
            return false;
        }
        this.autoMiner.getPlugin().getTokens().getApi().removeTokens(autoMinerGUI.getPlayer(), cost);
        addUpgrade(autoMinerGUI.getPlayer(), autoMinerGUI.getPickaxe(), upgrade.getId(), currentLevel + upgradeLevel);
        autoMinerGUI.getPlayer().getInventory().setItem(autoMinerGUI.getPickaxePlayerInventorySlot(), autoMinerGUI.getPickaxe());
        if (upgradeLevel == 1) {
            PlayerUtils.sendMessage(autoMinerGUI.getPlayer(), "&e&lAUTOMINER &8&c&l-%tokens% TOKENS".replace("%tokens%", String.valueOf(cost)));
        } else {
            PlayerUtils.sendMessage(autoMinerGUI.getPlayer(), "&e&lENCHANT &8&7You have bought &f%amount% &7levels of %enchant%&r &7for &f%tokens% &7tokens."
                    .replace("%amount%", String.valueOf(upgradeLevel))
                    .replace("%enchant%", upgrade.getName())
                    .replace("%tokens%", String.format("%,d", cost)));
        }
        return true;
    }

    public ItemStack addUpgrade(Player paramPlayer, ItemStack paramItemStack, int paramInt1, int paramInt2) {
        AutoMinerUpgrade upgrade = AutoMinerUpgrade.getUpgradeById(paramInt1);
        if (upgrade == null || paramItemStack == null)
            return paramItemStack;
        NBTItem nBTItem = new NBTItem(paramItemStack, true);
        if (paramInt2 > 0)
            nBTItem.setInteger("EvilPrison-Autominer-Upgrade-" + upgrade.getId(), paramInt2);
        if (!nBTItem.hasKey("pickaxe-id"))
            nBTItem.setString("pickaxe-id", UUID.randomUUID().toString());
        nBTItem.mergeCustomNBT(paramItemStack);
        return paramItemStack;
    }

    public Item getGuiItem(AutoMinerUpgrade upgrade, AutoMinerGUI autoMinerGUI, int paramInt) {
        ItemStackBuilder itemStackBuilder = ItemStackBuilder.of(upgrade.getMaterial());
        if (upgrade.getBase64() != null && !upgrade.getBase64().isEmpty())
            itemStackBuilder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(upgrade.getBase64()));
        itemStackBuilder.name(upgrade.getName());
        itemStackBuilder.lore(translateLore(upgrade, UPGRADE_GUI_ITEM_LORE, paramInt));
        return itemStackBuilder.buildItem().bind(paramInventoryClickEvent -> {
            if (paramInventoryClickEvent.getClick() == ClickType.MIDDLE || paramInventoryClickEvent.getClick() == ClickType.SHIFT_RIGHT) {
                buyUpgrade(upgrade, autoMinerGUI, paramInt, 100);
                autoMinerGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.LEFT) {
                buyUpgrade(upgrade, autoMinerGUI, paramInt, 1);
                autoMinerGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.RIGHT) {
                buyUpgrade(upgrade, autoMinerGUI, paramInt, 10);
                autoMinerGUI.redraw();
            } else if (paramInventoryClickEvent.getClick() == ClickType.DROP) {
                int upgrades = (int) Math.floor((float) (upgrade.getIncreaseCost() + 2 * (autoMiner.getPlugin().getTokens().getApi().getPlayerTokens(autoMinerGUI.getPlayer()))) / (2 * paramInt * upgrade.getIncreaseCost() + upgrade.getCost() + upgrade.getIncreaseCost()));
                buyUpgrade(upgrade, autoMinerGUI, paramInt, upgrades);
            }
        }, new ClickType[] { ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT, ClickType.DROP }).build();
    }

    private List<String> translateLore(AutoMinerUpgrade upgrade, List<String> paramList, int paramInt) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : paramList) {
            if (str.contains("%description%")) {
                arrayList.addAll(upgrade.getDescription());
                continue;
            }
            arrayList.add(str
                    .replace("%cost%", String.format("%,d", upgrade.getCost() + upgrade.getIncreaseCost() * paramInt))
                    .replace("%max_level%", (upgrade.getMaxLevel() == Integer.MAX_VALUE) ? "Unlimited" : String.format("%,d", upgrade.getMaxLevel()))
                    .replace("%current_level%", String.format("%,d", paramInt)));
        }
        return arrayList;
    }
}
