package me.pwo.evilprisoncore.pickaxe.pickaxeboosters.model;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.item.ItemStackBuilder;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import org.apache.commons.lang3.Range;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Booster {
    private static final String PICKAXE_BOOSTER_NBT_IDENTIFIER = "EvilPrison-Pickaxe-Booster";
    private final int id;
    private final String name;
    private final List<String> lore;
    private final Material material;
    private final int damage;
    private final Range<Double> range;
    private final List<EvilEnchant> blacklistedEnchants;

    public Booster(int id, String name, List<String> lore, Material material, int damage, Range<Double> range, List<EvilEnchant> blacklistedEnchants) {
        this.id = id;
        this.name = name;
        this.lore = lore;
        this.material = material;
        this.damage = damage;
        this.range = range;
        this.blacklistedEnchants = blacklistedEnchants;
    }

    public ItemStack getItem(int amount) {
        ItemStack itemStack = ItemStackBuilder.of(this.material)
                .name(this.name)
                .lore(this.lore)
                .amount(amount)
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS).build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(PICKAXE_BOOSTER_NBT_IDENTIFIER, this.id);
        return nbtItem.getItem();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }

    public int getDamage() {
        return damage;
    }

    public Range<Double> getRange() {
        return range;
    }

    public List<EvilEnchant> getBlacklistedEnchants() {
        return blacklistedEnchants;
    }
}
