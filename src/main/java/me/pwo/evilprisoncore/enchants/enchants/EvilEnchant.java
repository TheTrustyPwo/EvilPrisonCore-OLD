package me.pwo.evilprisoncore.enchants.enchants;

import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.impl.*;
import me.pwo.evilprisoncore.utils.TextUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class EvilEnchant implements Refundable {
    private static HashMap<Integer, EvilEnchant> allEnchantmentsById = new HashMap<>();
    private static HashMap<String, EvilEnchant> allEnchantmentsByName = new HashMap<>();
    protected final Enchants enchants;
    protected final int id;
    private String rawName;
    private String name;
    private String base64;
    private Material material;
    private List<String> description;
    private boolean enabled;
    private int guiSlot;
    private int maxLevel;
    private long cost;
    private long increaseCost;
    private int requiredPickaxeLevel;
    private boolean messagesEnabled;

    public Enchants getEnchants() {
        return this.enchants;
    }

    public int getId() {
        return this.id;
    }

    public String getRawName() {
        return this.rawName;
    }

    public String getName() {
        return this.name;
    }

    public String getBase64() {
        return this.base64;
    }

    public Material getMaterial() {
        return this.material;
    }

    public List<String> getDescription() {
        return this.description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public long getCost() {
        return this.cost;
    }

    public long getIncreaseCost() {
        return this.increaseCost;
    }

    public int getRequiredPickaxeLevel() {
        return this.requiredPickaxeLevel;
    }

    public boolean isMessagesEnabled() {
        return this.messagesEnabled;
    }

    public EvilEnchant(Enchants enchants, int paramInt) {
        this.enchants = enchants;
        this.id = paramInt;
        reloadDefaultAttributes();
        reload();
    }

    private void reloadDefaultAttributes() {
        this.rawName = this.enchants.getConfig().getString("enchants." + this.id + ".RawName");
        this.name = Text.colorize(this.enchants.getConfig().getString("enchants." + this.id + ".Name"));
        this.material = Material.getMaterial(this.enchants.getConfig().getString("enchants." + this.id + ".Material"));
        this.description = TextUtils.colorize(this.enchants.getConfig().getStringList("enchants." + this.id + ".Description"));
        this.enabled = this.enchants.getConfig().getBoolean("enchants." + this.id + ".Enabled");
        this.guiSlot = this.enchants.getConfig().getInt("enchants." + this.id + ".InGuiSlot");
        this.maxLevel = this.enchants.getConfig().getInt("enchants." + this.id + ".Max");
        this.cost = this.enchants.getConfig().getLong("enchants." + this.id + ".Cost");
        this.increaseCost = this.enchants.getConfig().getLong("enchants." + this.id + ".Increase-Cost-by");
        this.requiredPickaxeLevel = this.enchants.getConfig().getInt("enchants." + this.id + ".Pickaxe-Level-Required");
        this.messagesEnabled = this.enchants.getConfig().getBoolean("enchants." + this.id + ".Messages-Enabled", true);
        this.base64 = this.enchants.getConfig().getString("enchants." + this.id + ".Base64", null);
    }

    public static Collection<EvilEnchant> all() {
        return allEnchantmentsById.values();
    }

    public long getCostOfLevel(int paramInt) {
        return this.cost + this.increaseCost * (paramInt - 1);
    }

    public boolean isRefundEnabled() {
        return this.enchants.getConfig().getBoolean("enchants." + this.id + ".Refund.Enabled");
    }

    public int refundGuiSlot() {
        return this.enchants.getConfig().getInt("enchants." + this.id + ".Refund.InGuiSlot");
    }

    public static EvilEnchant getEnchantById(int paramInt) {
        return allEnchantmentsById.get(paramInt);
    }

    public static EvilEnchant getEnchantByName(String paramString) {
        return allEnchantmentsByName.get(paramString.toLowerCase());
    }

    public void register() {
        if (allEnchantmentsById.containsKey(getId()) || allEnchantmentsByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to register enchant " + getName() + ". That enchant is already registered."));
            return;
        }
        Validate.notNull(getRawName());
        allEnchantmentsById.put(getId(), this);
        allEnchantmentsByName.put(getRawName().toLowerCase(), this);
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully registered enchant " + getName()));
    }

    public void unregister() {
        if (!allEnchantmentsById.containsKey(getId()) && !allEnchantmentsByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to unregister enchant " + getName() + ". That enchant is not registered."));
            return;
        }
        allEnchantmentsById.remove(getId());
        allEnchantmentsByName.remove(getRawName());
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully unregistered enchant " + getName()));
    }

    public static void loadDefaultEnchantments() {
        (new EfficiencyEnchant(Enchants.getInstance())).register();
        (new FortuneEnchant(Enchants.getInstance())).register();
        (new HasteEnchant(Enchants.getInstance())).register();
        (new SpeedEnchant(Enchants.getInstance())).register();
        (new JumpBoostEnchant(Enchants.getInstance())).register();
        (new ShockwaveEnchant(Enchants.getInstance())).register();
        (new TokenMerchant(Enchants.getInstance())).register();
        (new KeyFinderEnchant(Enchants.getInstance())).register();
        (new NukeEnchant(Enchants.getInstance())).register();
        (new LightningEnchant(Enchants.getInstance())).register();
        (new ExplosiveEnchant(Enchants.getInstance())).register();
        (new CreditFinder(Enchants.getInstance())).register();
        (new SecondHand(Enchants.getInstance())).register();
        (new GemFinder(Enchants.getInstance())).register();
        (new VeinMiner(Enchants.getInstance())).register();
        (new LaserEnchant(Enchants.getInstance())).register();
    }

    public static void reloadAll() {
        allEnchantmentsById.values().forEach(enchantment -> {
            enchantment.reloadDefaultAttributes();
            enchantment.reload();
        });
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully reloaded all enchants."));
    }

    public int getMaxLevel() {
        return (this.maxLevel == -1) ? Integer.MAX_VALUE : this.maxLevel;
    }

    public boolean canBeBought(ItemStack paramItemStack) {
        return (this.enchants.getPlugin().getPickaxe().getPickaxeLevels().getPickaxeLevel(paramItemStack).getLevel() >= this.requiredPickaxeLevel);
    }

    public abstract void onEquip(Player player, ItemStack itemStack, int level);

    public abstract void onUnequip(Player player, ItemStack itemStack, int level);

    public abstract void onBlockBreak(BlockBreakEvent e, int level, double random);

    public abstract void reload();
}
