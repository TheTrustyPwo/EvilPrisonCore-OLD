package me.pwo.evilprisoncore.privatemines;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.EvilPrisonModules;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PrivateMines implements EvilPrisonModules {
    private static PrivateMines instance;
    private EvilPrisonCore plugin;
    private SlimePlugin slimePlugin;
    private FileConfiguration privateMinesConfig;
    private HashMap<UUID, Mine> mines;
    private SlimeWorld defaultWorld;
    private boolean enabled;

    public static PrivateMines getInstance() {
        return instance;
    }

    public EvilPrisonCore getPlugin() {
        return plugin;
    }

    public SlimePlugin getSlimePlugin() {
        return slimePlugin;
    }

    public FileConfiguration getPrivateMinesConfig() {
        return privateMinesConfig;
    }

    public HashMap<UUID, Mine> getMines() {
        return mines;
    }

    public PrivateMines(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        instance = this;
        this.privateMinesConfig = this.plugin.getFileUtils().getConfig("private-mines.yml").copyDefaults(true).save().get();
        this.slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        registerEvents();
        mines = new HashMap<>();
        loadDefaultWorld();
        loadAllMines();
        this.enabled = true;
    }

    private void loadAllMines() {
        ResultSet resultSet = this.plugin.getPluginDatabase().getAllMineData();
        try {
            while (resultSet.next()) {
                this.mines.put(UUID.fromString(resultSet.getString("UUID")), new Mine(
                        this, UUID.fromString(resultSet.getString("UUID"))));
                this.plugin.getLogger().info(String.format("Loaded %d Private Mines", this.mines.size()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultWorld() {
        SlimeLoader slimeLoader = this.slimePlugin.getLoader("mysql");
        SlimeWorld.SlimeProperties properties = SlimeWorld.SlimeProperties.builder()
                .difficulty(0)
                .allowAnimals(false)
                .allowMonsters(false)
                .spawnX(0)
                .spawnY(130)
                .spawnZ(0)
                .pvp(false)
                .readOnly(false)
                .build();
        try {
            this.defaultWorld = this.slimePlugin.loadWorld(slimeLoader, "default", properties);
        } catch (CorruptedWorldException | UnknownWorldException | IOException | NewerFormatException | WorldInUseException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    Schedulers.async().run(() -> {
                        try {
                            this.defaultWorld.clone("PMine-" + e.getPlayer().getUniqueId(), this.slimePlugin.getLoader("mysql"));
                        } catch (IOException | WorldAlreadyExistsException exception) {
                            exception.printStackTrace();
                        }
                        this.plugin.getPluginDatabase().addIntoMineData(e.getPlayer());
                        this.mines.put(e.getPlayer().getUniqueId(), new Mine(this, e.getPlayer().getUniqueId()));
                    });
                }).bindWith(this.plugin);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    Schedulers.async().run(() -> {
                        this.mines.remove(e.getPlayer().getUniqueId());
                    });
                }).bindWith(this.plugin);
    }

    public Mine locatePlayer(Player player) {
        for (Mine mine : this.mines.values()) {
        }
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.plugin.getFileUtils().getConfig("private-mines.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Private Mines";
    }
}
