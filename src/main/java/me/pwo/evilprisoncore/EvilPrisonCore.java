package me.pwo.evilprisoncore;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.database.Database;
import me.pwo.evilprisoncore.database.implementations.MySQLDatabase;
import me.pwo.evilprisoncore.database.implementations.SQLiteDatabase;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.events.Events;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.menu.Menu;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pickaxe.Pickaxe;
import me.pwo.evilprisoncore.placeholders.EvilPrisonPAPIPlaceholders;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.FileUtils;
import me.pwo.evilprisoncore.utils.MinecraftVersion;
import me.pwo.evilprisoncore.utils.SkullUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.units.qual.C;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class EvilPrisonCore extends ExtendedJavaPlugin {

    public static EvilPrisonCore instance;
    private LinkedHashMap<String, EvilPrisonModules> loadedModules;
    private Database pluginDatabase;
    private FileUtils fileUtils;
    private List<Material> pickaxesSupported;
    private Economy economy;

    private AutoMiner autoMiner;
    private Blocks blocks;
    private Tokens tokens;
    private Gems gems;
    private Credits credits;
    private Ranks ranks;
    private Enchants enchants;
    private Pickaxe pickaxe;
    private Menu menu;
    private Events events;
    private Pets pets;

    public LinkedHashMap<String, EvilPrisonModules> getLoadedModules() {
        return this.loadedModules;
    }

    public Database getPluginDatabase() {
        return this.pluginDatabase;
    }

    public FileUtils getFileUtils() {
        return this.fileUtils;
    }

    public List<Material> getPickaxesSupported() { return this.pickaxesSupported; }

    public Economy getEconomy() {
        return economy;
    }

    public Blocks getBlocks() {
        return blocks;
    }

    public Tokens getTokens() {
        return this.tokens;
    }

    public Gems getGems() {
        return gems;
    }

    public Credits getCredits() {
        return credits;
    }

    public Ranks getRanks() {
        return ranks;
    }

    public Enchants getEnchants() {
        return enchants;
    }

    public Pickaxe getPickaxe() {
        return pickaxe;
    }

    public AutoMiner getAutoMiner() {
        return autoMiner;
    }

    public Menu getMenu() {
        return menu;
    }

    public Events getEvents() {
        return events;
    }

    public Pets getPets() {
        return pets;
    }

    public static EvilPrisonCore getInstance() {
        return instance;
    }

    protected void enable() {
        MinecraftVersion.init();
        SkullUtils.init();
        instance = this;
        this.loadedModules = new LinkedHashMap<>();
        this.fileUtils = new FileUtils(this);
        this.fileUtils.getConfig("config.yml").copyDefaults(true).save();
        try {
            String databaseType = getConfig().getString("database-type");
            if (databaseType.equalsIgnoreCase("sqlite")) {
                this.pluginDatabase = new SQLiteDatabase(this);
            } else if (databaseType.equalsIgnoreCase("mysql")) {
                this.pluginDatabase = new MySQLDatabase(this);
            } else {
                getLogger().warning(String.format("Error! Unknown database type: %s. Disabling plugin.", databaseType));
                getServer().getPluginManager().disablePlugin(this);
            }
        } catch (Exception exception) {
            getLogger().warning("Could not maintain Database Connection. Disabling plugin.");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        this.pickaxesSupported = Collections.singletonList(Material.DIAMOND_PICKAXE);
        this.autoMiner = new AutoMiner(this);
        this.blocks = new Blocks(this);
        this.tokens = new Tokens(this);
        this.gems = new Gems(this);
        this.credits = new Credits(this);
        this.ranks = new Ranks(this);
        this.enchants = new Enchants(this);
        this.pickaxe = new Pickaxe(this);
        this.menu = new Menu(this);
        this.events = new Events(this);
        this.pets = new Pets(this);
        if (!setupEconomy()) {
            getLogger().warning("Economy provider for Vault not found! Economy provider is strictly required. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Economy provider for Vault found - " + getEconomy().getName());
        loadModule(this.autoMiner);
        loadModule(this.blocks);
        loadModule(this.tokens);
        loadModule(this.gems);
        loadModule(this.credits);
        loadModule(this.ranks);
        loadModule(this.enchants);
        loadModule(this.pickaxe);
        loadModule(this.menu);
        loadModule(this.events);
        loadModule(this.pets);
        registerEvents();
        registerCommands();
        registerPlaceholders();
        getLogger().info("EvilPrisonCore has enabled!");
    }

    public void loadModule(EvilPrisonModules module) {
        this.loadedModules.put(module.getName().toLowerCase(), module);
        module.enable();
        getLogger().info(Text.colorize(String.format("EvilPrisonCore - Module %s loaded.", module.getName())));
    }

    public void reloadModule(EvilPrisonModules module) {
        module.reload();
        getLogger().info(Text.colorize(String.format("EvilPrisonCore - Module %s reloaded.", module.getName())));
    }

    private void registerEvents() {
    }

    private void registerCommands() {
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            (new EvilPrisonPAPIPlaceholders(this)).register();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null)
            return false;
        this.economy = registeredServiceProvider.getProvider();
        return (this.economy != null);
    }

    public boolean isModuleEnabled(String moduleName) {
        return this.loadedModules.containsKey(moduleName.toLowerCase());
    }

    public boolean isPickaxeSupported(Material paramMaterial) {
        return this.pickaxesSupported.contains(paramMaterial);
    }

    public boolean isPickaxeSupported(ItemStack paramItemStack) {
        return isPickaxeSupported(paramItemStack.getType());
    }

    protected void disable() {
        getLogger().info("EvilPrisonCore has disabled!");
    }
}
