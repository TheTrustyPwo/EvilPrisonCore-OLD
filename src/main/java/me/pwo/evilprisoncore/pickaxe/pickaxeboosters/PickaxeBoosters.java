package me.pwo.evilprisoncore.pickaxe.pickaxeboosters;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.pickaxe.Pickaxe;
import me.pwo.evilprisoncore.pickaxe.pickaxeboosters.model.Booster;
import org.apache.commons.lang3.Range;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickaxeBoosters implements EvilPrisonModule {
    private static PickaxeBoosters instance;
    private final EvilPrisonCore plugin;
    private final Pickaxe pickaxe;
    private Map<Integer, Booster> boosters;
    private boolean enabled;

    public static PickaxeBoosters getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public Pickaxe getPickaxe() {
        return pickaxe;
    }

    public PickaxeBoosters(Pickaxe pickaxe) {
        this.pickaxe = pickaxe;
        this.plugin = this.pickaxe.getPlugin();
    }

    private void loadBoosters() {
        boosters = new HashMap<>();
        ConfigurationSection section = getPickaxe().getConfig().getConfigurationSection("boosters");
        if (section == null) return;
        for (String string : section.getKeys(false)) {
            String name = section.getString(string + ".Name");
            List<String> lore = section.getStringList(string + ".Lore");
            Material material = Material.getMaterial(section.getString(string + ".Material"));
            int damage = section.getInt(string + ".Damage");
            Range<Double> range = Range.between(
                    section.getDouble(string + ".Range.Min"),
                    section.getDouble(string + ".Range.Max")
            );
            List<EvilEnchant> blacklistedEnchants = new ArrayList<>();
            section.getStringList(string + ".BlacklistedEnchants")
                    .forEach((enchant) -> blacklistedEnchants.add(EvilEnchant.getEnchantByName(enchant)));
            boosters.put(Integer.parseInt(string), new Booster(Integer.parseInt(string), name, lore, material, damage, range, blacklistedEnchants));
            this.plugin.getLogger().info("Loaded Pickaxe Booster: " + string);
        }
    }

    private void registerEvents() {

    }

    private void registerCommands() {

    }

    @Override
    public void enable() {
        this.enabled = true;
        loadBoosters();
        registerEvents();
        registerCommands();
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
        return "Pickaxe Boosters";
    }
}
