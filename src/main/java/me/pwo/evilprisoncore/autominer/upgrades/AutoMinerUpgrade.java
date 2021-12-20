package me.pwo.evilprisoncore.autominer.upgrades;

import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.utils.TextUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class AutoMinerUpgrade {
    private static HashMap<Integer, AutoMinerUpgrade> allUpgradesById = new HashMap<>();
    private static HashMap<String, AutoMinerUpgrade> allUpgradesByName = new HashMap<>();
    protected final AutoMiner autoMiner;
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

    public AutoMiner getAutoMiner() {
        return this.autoMiner;
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

    public AutoMinerUpgrade(AutoMiner autoMiner, int id) {
        this.autoMiner = autoMiner;
        this.id = id;
        reloadDefaultAttributes();
        reload();
    }

    private void reloadDefaultAttributes() {
        this.rawName = this.autoMiner.getConfig().getString("autominer_upgrades." + this.id + ".RawName");
        this.name = Text.colorize(this.autoMiner.getConfig().getString("autominer_upgrades." + this.id + ".Name"));
        this.material = Material.getMaterial(this.autoMiner.getConfig().getString("autominer_upgrades." + this.id + ".Material"));
        this.description = TextUtils.colorize(this.autoMiner.getConfig().getStringList("enchants." + this.id + ".Description"));
        this.enabled = this.autoMiner.getConfig().getBoolean("autominer_upgrades." + this.id + ".Enabled");
        this.guiSlot = this.autoMiner.getConfig().getInt("autominer_upgrades." + this.id + ".InGuiSlot");
        this.maxLevel = this.autoMiner.getConfig().getInt("autominer_upgrades." + this.id + ".Max");
        this.cost = this.autoMiner.getConfig().getLong("autominer_upgrades." + this.id + ".Cost");
        this.increaseCost = this.autoMiner.getConfig().getLong("autominer_upgrades." + this.id + ".Increase-Cost-by");
        this.base64 = this.autoMiner.getConfig().getString("autominer_upgrades." + this.id + ".Base64", null);
    }

    public static Collection<AutoMinerUpgrade> all() {
        return allUpgradesById.values();
    }

    public static AutoMinerUpgrade getUpgradeById(int paramInt) {
        return allUpgradesById.get(paramInt);
    }

    public static AutoMinerUpgrade getUpgradeByName(String paramString) {
        return allUpgradesByName.get(paramString.toLowerCase());
    }

    public long getCostOfLevel(int paramInt) {
        return this.cost + this.increaseCost * (paramInt - 1);
    }

    public void register() {
        if (allUpgradesById.containsKey(getId()) || allUpgradesByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to register Autominer upgrade " + getName() + ". That upgrade is already registered."));
            return;
        }
        Validate.notNull(getRawName());
        allUpgradesById.put(getId(), this);
        allUpgradesByName.put(getRawName().toLowerCase(), this);
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully registered Autominer upgrade " + getName()));
    }

    public void unregister() {
        if (!allUpgradesById.containsKey(getId()) && !allUpgradesByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to unregister Autominer upgrade " + getName() + ". That upgrade is not registered."));
            return;
        }
        allUpgradesById.remove(getId());
        allUpgradesByName.remove(getRawName());
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully unregistered Autominer upgrade " + getName()));
    }

    public int getMaxLevel() {
        return (this.maxLevel == -1) ? Integer.MAX_VALUE : this.maxLevel;
    }

    public abstract void onAutoMine(Player player, int level);

    public abstract void reload();
}
