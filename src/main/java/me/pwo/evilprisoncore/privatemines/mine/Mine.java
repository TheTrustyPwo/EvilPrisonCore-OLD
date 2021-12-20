package me.pwo.evilprisoncore.privatemines.mine;

import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.ranks.Ranks;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Mine {
    private static final int autoResetInterval = 300;
    private static final double resetPercentage = 30.0D;
    private final PrivateMines privateMines;
    private final UUID owner;
    private World world;
    private SlimeWorld slimeWorld;
    private String worldName;
    private Location spawnLocation;
    private Location corner1;
    private Location corner2;
    private int mineSize;
    private boolean isPublic;
    private double tax;
    private Material material;
    private List<UUID> playersInMine;
    private List<UUID> playersWithAccess;
    private List<UUID> bannedPlayers;
    private Task resetTask;

    public Mine(PrivateMines privateMines, UUID owner) {
        this.privateMines = privateMines;
        this.owner = owner;
        this.worldName = "PMine-" + owner;
        this.mineSize = Ranks.getInstance().getApi().getPlayerRank(Players.getNullable(owner)).getMineSize();
        updateCornerLocation();
        build();
        startAutoResetTask();
    }

    public UUID getOwner() {
        return owner;
    }

    public World getWorld() {
        return world;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void togglePublic() { this.isPublic = !this.isPublic; }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public int getMineSize() {
        return mineSize;
    }

    public void givePlayerAccess(OfflinePlayer player) {
        this.privateMines.getPlugin().getPluginDatabase().givePlayerPrivateMineAccess(Players.getOfflineNullable(owner), player);
    }

    public void revokePlayerAccess(OfflinePlayer player) {
        this.privateMines.getPlugin().getPluginDatabase().revokePlayerPrivateMineAccess(Players.getOfflineNullable(owner), player);
    }

    public void addPlayer(Player player) {
        if (!this.bannedPlayers.contains(player.getUniqueId()) && this.playersInMine.size() <= 10) {
            this.playersInMine.add(player.getUniqueId());
            player.teleport(this.spawnLocation);
            Events.subscribe(PlayerQuitEvent.class)
                    .expireAfter(1)
                    .filter(e -> e.getPlayer().equals(player))
                    .handler(e -> this.playersInMine.remove(player.getUniqueId()));
        }
    }

    public void build() {
        // Load Mine data from Database
        ResultSet resultSet = this.privateMines.getPlugin().getPluginDatabase().getPlayerMineData(Players.getOfflineNullable(owner));
        try {
            this.isPublic = resultSet.getBoolean("Public");
            this.tax = resultSet.getDouble("Tax");
            this.material = Material.getMaterial(resultSet.getString("Material"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Load Slime World
        SlimeWorld.SlimeProperties properties = SlimeWorld.SlimeProperties.builder()
                .difficulty(0)
                .allowAnimals(false)
                .allowMonsters(false)
                .spawnX(spawnLocation.getX())
                .spawnY(spawnLocation.getY())
                .spawnZ(spawnLocation.getZ())
                .pvp(false)
                .readOnly(false)
                .build();
        try {
            this.slimeWorld = privateMines.getSlimePlugin().loadWorld(
                    privateMines.getSlimePlugin().getLoader("mysql"),
                    worldName,
                    properties);
        } catch (CorruptedWorldException | UnknownWorldException | IOException | NewerFormatException | WorldInUseException e) {
            e.printStackTrace();
        }
        this.world = Bukkit.getWorld(worldName);
        this.spawnLocation = new Location(world, 0.5D, 130.0D, 0.5D, 90.0f, 90.0f);
    }

    public void startAutoResetTask() {
        this.resetTask = Schedulers.async().runRepeating(this::reset, 30L, TimeUnit.SECONDS, autoResetInterval, TimeUnit.SECONDS);
    }

    public void cancelResetTask() {
        this.resetTask.close();
    }

    public void reset() {
        playersInMine.forEach((p) -> Players.getNullable(p).teleport(spawnLocation));
        com.sk89q.worldedit.world.World world = BukkitUtil.getLocalWorld(this.world);
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
            CuboidRegion cuboidRegion = new CuboidRegion(BukkitUtil.toVector(this.corner1), BukkitUtil.toVector(this.corner2));
            List<BlockChance> list = new ArrayList<>();
            list.add(new BlockChance(new BaseBlock(material.getId()), 99.0D));
            list.add(new BlockChance(new BaseBlock(Material.ENDER_STONE.getId()), 1.0D));
            RandomFillPattern randomFillPattern = new RandomFillPattern(list);
            editSession.setBlocks(cuboidRegion, randomFillPattern);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private void updateCornerLocation() {
        int rad = (this.mineSize - 1) / 2;
        this.corner1 = new Location(this.world, rad, 126, -rad);
        this.corner2 = new Location(this.world, -rad, 20, rad);
    }

    public void setMineSize(int size) {
        this.mineSize = size;
        updateCornerLocation();
        int rad = (size - 1) / 2;
        CuboidRegion cuboidRegion = new CuboidRegion(
                BukkitUtil.toVector(new Location(this.world, rad + 1, 126, -rad - 1)),
                BukkitUtil.toVector(new Location(this.world, -rad - 1, 20, rad + 1)));
        com.sk89q.worldedit.world.World world = BukkitUtil.getLocalWorld(this.world);
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
            editSession.makeCuboidWalls(cuboidRegion, new BaseBlock(Material.BEDROCK.getId()));
            reset();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }
}
