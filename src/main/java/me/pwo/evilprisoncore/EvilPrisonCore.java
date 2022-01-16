package me.pwo.evilprisoncore;

import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.autominer.AutoMiner;
import me.pwo.evilprisoncore.autosell.AutoSell;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.database.Database;
import me.pwo.evilprisoncore.database.SQLDatabase;
import me.pwo.evilprisoncore.database.implementations.MySQLDatabase;
import me.pwo.evilprisoncore.database.implementations.SQLiteDatabase;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.events.Events;
import me.pwo.evilprisoncore.gangs.EvilPrisonGangs;
import me.pwo.evilprisoncore.gems.Gems;
import me.pwo.evilprisoncore.menu.Menu;
import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.pets.Pets;
import me.pwo.evilprisoncore.pickaxe.Pickaxe;
import me.pwo.evilprisoncore.placeholders.EvilPrisonPAPIPlaceholders;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.FileUtils;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.SkullUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public final class EvilPrisonCore extends ExtendedJavaPlugin {
    public static boolean DEBUG = true;
    public static EvilPrisonCore instance;
    private Map<String, EvilPrisonModules> loadedModules;
    private Database pluginDatabase;
    private FileUtils fileUtils;
    private List<Material> pickaxesSupported;
    private Economy economy;

    private AutoMiner autoMiner;
    private AutoSell autoSell;
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
    private Multipliers multipliers;
    private PrivateMines privateMines;
    private EvilPrisonGangs gangs;

    public Map<String, EvilPrisonModules> getLoadedModules() {
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

    public AutoSell getAutoSell() {
        return autoSell;
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

    public Multipliers getMultipliers() {
        return multipliers;
    }

    public PrivateMines getPrivateMines() {
        return privateMines;
    }

    public EvilPrisonGangs getGangs() {
        return gangs;
    }

    public static EvilPrisonCore getInstance() {
        return instance;
    }

    protected void enable() {
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
        this.autoSell = new AutoSell(this);
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
        this.multipliers = new Multipliers(this);
        this.privateMines = new PrivateMines(this);
        this.gangs = new EvilPrisonGangs(this);
        if (!setupEconomy()) {
            getLogger().warning("Economy provider for Vault not found! Economy provider is strictly required. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Economy provider for Vault found - " + getEconomy().getName());
        loadAllModules();
        registerEvents();
        registerCommands();
        registerPlaceholders();
        getLogger().info("EvilPrisonCore has enabled!");
    }

    private void loadAllModules() {
        loadModule(this.autoMiner);
        loadModule(this.autoSell);
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
        loadModule(this.multipliers);
        loadModule(this.privateMines);
        loadModule(this.gangs);
    }

    public void loadModule(EvilPrisonModules module) {
        this.loadedModules.put(module.getName().toLowerCase(), module);
        module.enable();
        getLogger().info(Text.colorize(String.format("EvilPrisonCore - Module %s loaded.", module.getName())));
    }

    public void unloadModule(EvilPrisonModules modules) {
        this.loadedModules.remove(modules.getName().toLowerCase());
        modules.disable();
        getLogger().info(Text.colorize(String.format("EvilPrisonCore - Module %s unloaded.", modules.getName())));
    }

    public void reloadModule(EvilPrisonModules module) {
        module.reload();
        getLogger().info(Text.colorize(String.format("EvilPrisonCore - Module %s reloaded.", module.getName())));
    }

    public void reload() {
        this.loadedModules.values().forEach(this::reloadModule);
    }

    private void registerEvents() {
    }

    private void registerCommands() {
        Commands.create()
                .assertPermission("evilprison.admin", "&c&l(!) &cNo Permission")
                .handler(context -> {
                    if (context.args().size() == 0) return;
                    if (context.rawArg(0).equalsIgnoreCase("reload")) {
                        if (context.args().size() == 1) {
                            reload();
                        } else if (context.args().size() == 2) {
                            EvilPrisonModules modules = getModuleByName(context.rawArg(1));
                            if (modules == null) {
                                PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cModule %module% is not loaded"
                                        .replaceAll("%module%", context.rawArg(1)), true);
                                return;
                            }
                            reloadModule(modules);
                            PlayerUtils.sendMessage(context.sender(), "&aModule %module% reloaded!"
                                    .replaceAll("%module%", context.rawArg(1)), true);
                        }
                    } else if (context.rawArg(0).equalsIgnoreCase("debug")) {
                        DEBUG = !DEBUG;
                        PlayerUtils.sendMessage(context.sender(), "&eDebug Mode: %debug%"
                                .replaceAll("%debug%", DEBUG ? "&a&lON" : "&c&lOFF"), true);
                    } else if (context.rawArg(0).equalsIgnoreCase("info")) {
                        PlayerUtils.sendMessage(context.sender(), getDescription().getFullName());
                    }
                }).registerAndBind(this, "evilprisoncore", "evilprison", "epc", "ep", "prison", "prisoncore");
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

    public EvilPrisonModules getModuleByName(String paramString) {
        if (!isModuleEnabled(paramString))
            return null;
        return this.loadedModules.get(paramString.toLowerCase());
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

    public void debug(String debug) {
        if (DEBUG) getLogger().info(Text.colorize(debug));
    }

    private void unloadAllModules() {
        unloadModule(this.autoMiner);
        unloadModule(this.autoSell);
        unloadModule(this.blocks);
        unloadModule(this.tokens);
        unloadModule(this.gems);
        unloadModule(this.credits);
        unloadModule(this.ranks);
        unloadModule(this.enchants);
        unloadModule(this.pickaxe);
        unloadModule(this.menu);
        unloadModule(this.events);
        unloadModule(this.pets);
        unloadModule(this.multipliers);
        unloadModule(this.privateMines);
        unloadModule(this.gangs);
    }

    protected void disable() {
        unloadAllModules();
        ((SQLDatabase) this.pluginDatabase).close();
        getLogger().info("EvilPrisonCore has disabled!");
    }
}
