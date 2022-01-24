package me.pwo.evilprisoncore.autosell;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.autosell.api.AutoSellAPI;
import me.pwo.evilprisoncore.autosell.api.AutoSellAPIImpl;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class AutoSell implements EvilPrisonModule {
    private final EvilPrisonCore plugin;
    private FileConfiguration autosellConfig;
    private AutoSellAPI autoSellAPI;
    private Map<Material, Double> sellItems;
    private final HashMap<UUID, Double> lastEarnings = new HashMap<>();
    private final HashMap<UUID, Long> lastItems = new HashMap<>();
    private final List<UUID> enabledAutoSell = new ArrayList<>();
    private List<String> whitelistedWorlds = new ArrayList<>();
    private boolean enabled;

    public AutoSellAPI getApi() {
        return autoSellAPI;
    }

    public AutoSell(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.autosellConfig = this.plugin.getFileUtils().getConfig("autosell.yml").copyDefaults(true).save().get();
        this.whitelistedWorlds = this.autosellConfig.getStringList("whitelisted-worlds");
        this.autoSellAPI = new AutoSellAPIImpl(this);
        loadSellItems();
        registerEvents();
        registerCommands();
        runBroadcastTask();
        this.enabled = true;
    }

    private void loadSellItems() {
        this.sellItems = new HashMap<>();
        ConfigurationSection section = this.autosellConfig.getConfigurationSection("items");
        for (String string : section.getKeys(false)) {
            Material material = Material.getMaterial(string);
            Double price = section.getDouble(string);
            this.sellItems.put(material, price);
        }
    }

    private void runBroadcastTask() {
        Schedulers.async().runRepeating(() -> {
            Players.all().stream().filter((player) -> this.lastEarnings.containsKey(player.getUniqueId())).forEach((player) -> {
                double lastEarnings = this.lastEarnings.getOrDefault(player.getUniqueId(), 0.0D);
                if (lastEarnings > 0.0D) {
                    long lastItems = this.lastItems.getOrDefault(player.getUniqueId(), 0L);
                    PlayerUtils.sendMessage(player, Arrays.asList(
                            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
                            " &8&l» &6&lAUTOSELL:",
                            " &8&l➥ &e&lMONEY MADE: &2$&a%money%".replaceAll("%money%", Utils.formatNumber(lastEarnings)),
                            " &8&l➥ &e&lITEMS SOLD: &f%items%".replaceAll("%items%", Utils.formatNumber(lastItems)),
                            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"), false);
                }
            });
            this.lastEarnings.clear();
            this.lastItems.clear();
        }, 60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().runLater(() -> {
                    if (e.getPlayer().hasPermission("evilprison.autosell.toggle") && !this.enabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        this.toggleAutoSell(e.getPlayer());
                    } else if (this.enabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        PlayerUtils.sendMessage(e.getPlayer(), "&e&l(!) &eYou've &a&lENABLED &eAutosell.");
                    }
                }, 20L)).bindWith(this.plugin);
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> (e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE))
                .filter(e -> this.whitelistedWorlds.contains(e.getPlayer().getWorld().getName()))
                .handler(e -> {
                    int fortune = this.plugin.getEnchants().getEnchantsManager().getEnchantLevel(e.getPlayer().getItemInHand(), 3) + 1;
                    double price = this.sellItems.get(e.getBlock().getType());
                    price = this.plugin.getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), price * fortune, MultiplierType.MONEY);
                    long items = (long) e.getBlock().getDrops(e.getPlayer().getItemInHand()).size() * fortune;
                    this.plugin.getEconomy().depositPlayer(e.getPlayer(), price);
                    this.lastEarnings.put(e.getPlayer().getUniqueId(), price);
                    this.lastItems.put(e.getPlayer().getUniqueId(), this.lastItems.getOrDefault(e.getPlayer().getUniqueId(), 0L) + items);
                    e.getBlock().getDrops().clear();
                    e.getBlock().setType(Material.AIR);
                }).bindWith(this.plugin);
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .assertPermission("evilprison.autosell.toggle", "&c&l(!) &cNo Permission")
                .handler(context -> {
                    if (context.args().size() == 0) toggleAutoSell(context.sender());
                }).registerAndBind(this.plugin, "autosell", "as");
        Commands.create()
                .assertPlayer()
                .assertPermission("evilprison.autosell.admin", "&c&l(!) &cNo Permission")
                .handler(context -> {
                    if (context.args().size() == 1) {
                        Material material = context.sender().getItemInHand().getType();
                        Double price = context.arg(0).parseOrFail(Double.class);
                        if (material == null || price < 0.0D) return;
                        this.autosellConfig.set("items" + material.name(), price);
                        this.sellItems.put(material, price);
                        PlayerUtils.sendMessage(context.sender(),
                                "&aSuccessfully set sell price of &e%material% &ato &e%price%&a."
                                        .replaceAll("%material%", material.name())
                                        .replaceAll("%price%", Utils.formatNumber(price)));
                    }
                }).registerAndBind(this.plugin, "sellprice", "sp");
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    if (!this.whitelistedWorlds.contains(context.sender().getWorld().getName())) {
                        PlayerUtils.sendMessage(context.sender(), "&c&l(!) &cAutosell disabled in current world");
                        return;
                    }
                    double money = 0.0D;
                    ArrayList<ItemStack> list = new ArrayList<>();
                    for (Material material : this.sellItems.keySet()) {
                        for (ItemStack itemStack : Arrays.stream(context.sender().getInventory().getContents()).filter(paramItemStack -> (paramItemStack != null && paramItemStack.getType() == material)).collect(Collectors.toList())) {
                            money += itemStack.getAmount() * this.sellItems.get(material);
                            list.add(itemStack);
                        }
                    }
                    list.forEach(itemStack -> context.sender().getInventory().removeItem(itemStack));
                    money = this.plugin.getMultipliers().getApi().getTotalToDeposit(context.sender(), money, MultiplierType.MONEY);
                    this.plugin.getEconomy().depositPlayer(context.sender(), money);
                    PlayerUtils.sendMessage(context.sender(), "&eYou've sold your inventory for &2$&a%price%&e!"
                            .replaceAll("%price%", Utils.formatNumber(money)), true);
                }).registerAndBind(this.plugin, "sellall");
    }

    private void toggleAutoSell(Player paramPlayer) {
        if (!this.enabledAutoSell.contains(paramPlayer.getUniqueId())) {
            PlayerUtils.sendMessage(paramPlayer, "&e&l(!) &eYou've &a&lENABLED &eAutosell.");
            this.enabledAutoSell.add(paramPlayer.getUniqueId());
        } else {
            this.enabledAutoSell.remove(paramPlayer.getUniqueId());
            PlayerUtils.sendMessage(paramPlayer, "&e&l(!) &eYou've &c&lDISABLED &eAutosell.");
        }
    }

    public double getCurrentEarnings(Player player) {
        return this.lastEarnings.getOrDefault(player.getUniqueId(), 0.0D);
    }

    public double getPriceForBrokenBlock(Material material) {
        return this.sellItems.get(material);
    }

    public boolean hasAutoSellEnabled(Player player) {
        return this.enabledAutoSell.contains(player.getUniqueId());
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("autosell.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Autosell";
    }
}
