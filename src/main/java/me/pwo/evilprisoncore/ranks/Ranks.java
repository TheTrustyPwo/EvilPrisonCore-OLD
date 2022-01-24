package me.pwo.evilprisoncore.ranks;

import me.lucko.helper.Commands;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.ranks.api.RanksAPI;
import me.pwo.evilprisoncore.ranks.api.RanksAPIImpl;
import me.pwo.evilprisoncore.ranks.manager.RankManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Ranks implements EvilPrisonModule {
    private static Ranks instance;
    private final EvilPrisonCore plugin;
    private RankManager rankManager;
    private RanksAPI ranksAPI;
    private FileConfiguration ranksConfig;
    private HashMap<String, String> messages;
    private List<UUID> maxRankingUpPlayers;
    private boolean enabled;

    public static Ranks getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public RanksAPI getApi() {
        return ranksAPI;
    }

    public FileConfiguration getConfig() {
        return ranksConfig;
    }

    public Ranks(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.ranksConfig = plugin.getFileUtils().getConfig("ranks.yml").copyDefaults(true).save().get();
        this.rankManager = new RankManager(this);
        this.ranksAPI = new RanksAPIImpl(this);
        loadMessages();
        this.maxRankingUpPlayers = new ArrayList<>(10);
        registerCommands();
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.rankManager.stopUpdating();
        this.rankManager.saveAllDataSync();
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("ranks.yml").reload();
        loadMessages();
        this.rankManager.reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    if (context.args().size() == 0)
                        this.rankManager.buyNextRank(context.sender());
                }).registerAndBind(this.plugin, "rankup");
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    if (context.args().size() == 0) {
                        if (this.maxRankingUpPlayers.contains((context.sender()).getUniqueId()))
                            return;
                        Schedulers.async().run(() -> {
                            this.maxRankingUpPlayers.add((context.sender()).getUniqueId());
                            this.rankManager.buyMaxRank(context.sender());
                            this.maxRankingUpPlayers.remove((context.sender()).getUniqueId());
                        });
                    }
                }).registerAndBind(this.plugin, "maxrankup", "rankupmax", "mru");
        Commands.create()
                .handler(context -> {
                    if (context.args().size() == 0)
                        this.rankManager.sendRankTop(context.sender());
                }).registerAndBind(this.plugin, "ranktop", "rankstop");
    }

    private void loadMessages() {
        this.messages = new HashMap<>();
        for (String str : this.ranksConfig.getConfigurationSection("messages").getKeys(false))
            this.messages.put(str.toLowerCase(), Text.colorize(this.ranksConfig.getString("messages." + str)));
    }

    public String getMessage(String paramString) {
        return this.messages.getOrDefault(paramString.toLowerCase(), Text.colorize("&cMessage " + paramString + " not found."));
    }

    @Override
    public String getName() {
        return "Ranks";
    }
}
