package me.pwo.evilprisoncore.pets.pets;

import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pets.model.PetTier;
import me.pwo.evilprisoncore.pets.pets.impl.MoneyPet;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class EvilPet {
    private static HashMap<Integer, EvilPet> allPetsById = new HashMap<>();
    private static HashMap<String, EvilPet> allPetsByName = new HashMap<>();
    protected final Pets pets;
    protected final int id;
    private String rawName;
    private String name;
    private String base64;
    private String primaryColor;
    private String secondaryColor;
    private List<String> description;

    public Pets getPets() {
        return pets;
    }

    public int getId() {
        return id;
    }

    public String getRawName() {
        return rawName;
    }

    public String getName() {
        return name;
    }

    public String getBase64() {
        return base64;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public List<String> getDescription() {
        return description;
    }

    public EvilPet(Pets pets, int id) {
        this.pets = pets;
        this.id = id;
        reloadDefaultAttributes();
        reload();
    }

    private void reloadDefaultAttributes() {
        this.rawName = this.pets.getConfig().getString("pets." + this.id + ".RawName");
        this.name = this.pets.getConfig().getString("pets." + this.id + ".Name");
        this.base64 = this.pets.getConfig().getString("pets." + this.id + ".Base64");
        this.primaryColor = this.pets.getConfig().getString("pets." + this.id + ".Primary-Color");
        this.secondaryColor = this.pets.getConfig().getString("pets." + this.id + ".Secondary-Color");
        this.description = this.pets.getConfig().getStringList("pets." + this.id + ".Description");
    }

    public static Collection<EvilPet> all() { return allPetsById.values(); }

    public static EvilPet getPetById(int id) { return allPetsById.get(id); }

    public static EvilPet getPetByName(String name) { return allPetsByName.get(name.toLowerCase()); }

    public void register() {
        if (allPetsById.containsKey(getId()) || allPetsByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to register pet " + getName() + ". That pet is already registered."));
            return;
        }
        allPetsById.put(getId(), this);
        allPetsByName.put(getRawName().toLowerCase(), this);
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully registered pet " + getName()));
    }

    public void unregister() {
        if (!allPetsById.containsKey(getId()) && !allPetsByName.containsKey(getRawName())) {
            EvilPrisonCore.getInstance().getLogger().warning(Text.colorize("&cUnable to unregister pet " + getName() + ". That pet is not registered."));
            return;
        }
        allPetsById.remove(getId());
        allPetsByName.remove(getRawName());
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully unregistered pet " + getName()));
    }

    public static void loadDefaultPets() {
        (new MoneyPet(Pets.getInstance())).register();
    }

    public static void reloadAll() {
        allPetsById.values().forEach(pets -> {
            pets.reloadDefaultAttributes();
            pets.reload();
        });
        EvilPrisonCore.getInstance().getLogger().info(Text.colorize("&aSuccessfully reloaded all pets."));
    }


    public abstract void onEnable(Player player, ItemStack itemStack, int level);

    public abstract void onDisable(Player player, ItemStack itemStack, int level);

    public abstract void onLevelUp(Player player, ItemStack itemStack, int level);

    public abstract void onTierUp(Player player, ItemStack itemStack, PetTier tier);

    public abstract void onBlockBreak(BlockBreakEvent e, int level);

    public abstract void reload();
}
