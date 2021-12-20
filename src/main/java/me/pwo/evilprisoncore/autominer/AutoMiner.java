package me.pwo.evilprisoncore.autominer;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.autominer.api.AutoMinerAPI;
import me.pwo.evilprisoncore.autominer.api.AutoMinerAPIImpl;
import me.pwo.evilprisoncore.autominer.manager.AutoMinerManager;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoMiner implements EvilPrisonModules {

    private final EvilPrisonCore plugin;
    private static AutoMiner instance;
    private AutoMinerManager autoMinerManager;
    private FileConfiguration autominerConfig;
    private HashMap<String, String> messages;
    private HashMap<UUID, Integer> autoMinerTimes;
    private AutoMinerRegion region;
    private AutoMinerAPI api;
    private List<UUID> disabledAutoMiner;
    private boolean enabled;

    public static AutoMiner getInstance() {
        return instance;
    }

    public AutoMinerManager getAutoMinerManager() {
        return autoMinerManager;
    }

    public FileConfiguration getConfig() {
        return this.autominerConfig;
    }

    public AutoMinerRegion getRegion() {
        return this.region;
    }

    public EvilPrisonCore getPlugin() {
        return this.plugin;
    }

    public AutoMinerAPI getApi() {
        return this.api;
    }

    public AutoMiner(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void reload() {
        this.plugin.getFileUtils().getConfig("autominer.yml").reload();
        loadMessages();
        loadAutoMinerRegion();
    }

    public void enable() {
        this.enabled = true;
        instance = this;
        this.autoMinerManager = new AutoMinerManager(this);
        this.autominerConfig = this.plugin.getFileUtils().getConfig("autominer.yml").copyDefaults(true).save().get();
        this.autoMinerTimes = new HashMap<>();
        this.disabledAutoMiner = new ArrayList<>();
        registerCommands();
        registerEvents();
        loadMessages();
        removeExpiredAutoMiners();
        loadAutoMinerRegion();
        loadPlayersAutoMiner();
        this.api = new AutoMinerAPIImpl(this);
    }

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(paramPlayerQuitEvent -> saveAutoMiner(paramPlayerQuitEvent.getPlayer(), true)).bindWith(this.plugin);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(paramPlayerJoinEvent -> loadAutoMiner(paramPlayerJoinEvent.getPlayer())).bindWith(this.plugin);
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(this::loadAutoMiner);
    }

    private void removeExpiredAutoMiners() {
        Schedulers.async().run(() -> {
            this.plugin.getPluginDatabase().removeExpiredAutoMiners();
            this.plugin.getLogger().info("Removed expired AutoMiners from database");
        });
    }

    private void loadAutoMiner(Player paramPlayer) {
        Schedulers.async().run(() -> {
            int i = this.plugin.getPluginDatabase().getPlayerAutoMinerTime(paramPlayer);
            this.autoMinerTimes.put(paramPlayer.getUniqueId(), i);
            this.plugin.getLogger().info(String.format("Loaded %s's AutoMiner Time.", paramPlayer.getName()));
        });
    }

    private void saveAutoMiner(Player paramPlayer, boolean paramBoolean) {
        int i = this.autoMinerTimes.getOrDefault(paramPlayer.getUniqueId(), 0);
        if (paramBoolean) {
            Schedulers.async().run(() -> {
                this.plugin.getPluginDatabase().saveAutoMiner(paramPlayer, i);
                this.autoMinerTimes.remove(paramPlayer.getUniqueId());
                this.plugin.getLogger().info(String.format("Saved %s's AutoMiner time.", paramPlayer.getName()));
            });
        } else {
            this.plugin.getPluginDatabase().saveAutoMiner(paramPlayer, i);
            this.autoMinerTimes.remove(paramPlayer.getUniqueId());
            this.plugin.getLogger().info(String.format("Saved %s's AutoMiner time.", paramPlayer.getName()));
        }
    }

    private void loadAutoMinerRegion() {
        String str1 = getConfig().getString("auto-miner-region.world");
        String str2 = getConfig().getString("auto-miner-region.name");
        List<String> list = getConfig().getStringList("auto-miner-region.rewards");
        int i = getConfig().getInt("auto-miner-region.reward-period");
        World world = Bukkit.getWorld(str1);
        if (world == null)
            return;
        if (i <= 0) {
            this.plugin.getLogger().warning("reward-period in autominer.yml needs to be greater than 0!");
            return;
        }
        Optional<IWrappedRegion> optional = WorldGuardWrapper.getInstance().getRegion(world, str2);
        if (!optional.isPresent()) {
            this.plugin.getLogger().warning(String.format("There is no such region named %s in world %s!", str2, world.getName()));
            return;
        }
        this.region = new AutoMinerRegion(this, world, optional.get(), list, i);
        this.plugin.getLogger().info("AutoMiner region loaded!");
    }

    private void loadMessages() {
        this.messages = new HashMap<>();
        for (String str : getConfig().getConfigurationSection("messages").getKeys(false))
            this.messages.put(str.toLowerCase(), Text.colorize(getConfig().getString("messages." + str)));
    }

    public void disable() {
        Players.all().forEach(paramPlayer -> saveAutoMiner(paramPlayer, false));
        this.enabled = false;
    }

    public String getName() {
        return "Auto Miner";
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 0)
                        PlayerUtils.sendMessage(paramCommandContext.sender(), this.messages.get("auto_miner_time").replace("%time%", getTimeLeft(paramCommandContext.sender())));
                }).registerAndBind(this.plugin, "miner", "autominer");
        Commands.create()
                .assertPermission("evilprison.autominer.admin")
                .handler(paramCommandContext -> {
                    if (paramCommandContext.args().size() == 4 && paramCommandContext.rawArg(0).equalsIgnoreCase("give")) {
                        TimeUnit timeUnit;
                        Player player = paramCommandContext.arg(1).parseOrFail(Player.class);
                        long l = paramCommandContext.arg(2).parseOrFail(Long.class);
                        try {
                            timeUnit = TimeUnit.valueOf(paramCommandContext.rawArg(3).toUpperCase());
                        } catch (IllegalArgumentException illegalArgumentException) {
                            PlayerUtils.sendMessage(paramCommandContext.sender(), Text.colorize("&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ",")));
                            return;
                        }
                        givePlayerAutoMinerTime(paramCommandContext.sender(), player, l, timeUnit);
                    }
                }).registerAndBind(this.plugin, "adminautominer", "aam");
    }

    private void givePlayerAutoMinerTime(CommandSender paramCommandSender, Player paramPlayer, long paramLong, TimeUnit paramTimeUnit) {
        if (paramPlayer == null || !paramPlayer.isOnline()) {
            PlayerUtils.sendMessage(paramCommandSender, Text.colorize("&cPlayer is not online!"));
            return;
        }
        int i = this.autoMinerTimes.getOrDefault(paramPlayer.getUniqueId(), 0);
        i = (int)(i + paramTimeUnit.toSeconds(paramLong));
        this.autoMinerTimes.put(paramPlayer.getUniqueId(), i);
        PlayerUtils.sendMessage(paramCommandSender, this.messages.get("auto_miner_time_add").replace("%time%", String.valueOf(paramLong)).replace("%timeunit%", paramTimeUnit.name()).replace("%player%", paramPlayer.getName()));
    }

    public boolean hasAutoMinerTime(Player paramPlayer) {
        return (this.autoMinerTimes.containsKey(paramPlayer.getUniqueId()) && this.autoMinerTimes.get(paramPlayer.getUniqueId()) > 0);
    }

    public void decrementTime(Player paramPlayer) {
        int i = this.autoMinerTimes.get(paramPlayer.getUniqueId()) - 1;
        this.autoMinerTimes.put(paramPlayer.getUniqueId(), i);
    }

    public String getMessage(String paramString) {
        return this.messages.get(paramString.toLowerCase());
    }

    public String getTimeLeft(Player paramPlayer) {
        if (!this.autoMinerTimes.containsKey(paramPlayer.getUniqueId()))
            return "0s";
        int i = this.autoMinerTimes.get(paramPlayer.getUniqueId());
        long l1 = (i / 86400);
        i = (int)(i - l1 * 86400L);
        long l2 = (i / 3600);
        i = (int)(i - l2 * 3600L);
        long l3 = (i / 60);
        i = (int)(i - l3 * 60L);
        long l4 = i;
        return l1 + "d " + l2 + "h " + l3 + "m " + l4 + "s";
    }

    private void toggleAutoMiner(Player paramPlayer) {
        if (this.disabledAutoMiner.contains(paramPlayer.getUniqueId())) {
            PlayerUtils.sendMessage(paramPlayer, getMessage("autominer_enabled"));
            this.disabledAutoMiner.remove(paramPlayer.getUniqueId());
        } else {
            PlayerUtils.sendMessage(paramPlayer, getMessage("autominer_disabled"));
            this.disabledAutoMiner.add(paramPlayer.getUniqueId());
        }
    }

    public boolean hasAutominerOff(Player paramPlayer) {
        return this.disabledAutoMiner.contains(paramPlayer.getUniqueId());
    }

    public int getAutoMinerTime(Player paramPlayer) {
        return this.autoMinerTimes.getOrDefault(paramPlayer.getUniqueId(), 0);
    }

    public boolean isInAutoMinerRegion(Player paramPlayer) {
        if (this.region == null)
            return false;
        return this.region.getRegion().contains(paramPlayer.getLocation());
    }
}
