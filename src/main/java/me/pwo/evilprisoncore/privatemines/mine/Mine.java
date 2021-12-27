package me.pwo.evilprisoncore.privatemines.mine;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.data.MineData;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Mine {
    public static final List<BlockVector> EXPANSION_VECTORS = Arrays.asList(BlockVector.UNIT_X, BlockVector.UNIT_Z);
    public static final BlockVector positiveY = BlockVector.UNIT_Y;
    private static final int AUTO_RESET_INTERVAL = 300;
    private static final double AUTO_RESET_PERCENTAGE = 30.0D;
    private static final int MAX_WHITELISTED_PLAYERS = 9;
    private final World world = Bukkit.getWorld("private-mines");
    private final PrivateMines privateMines;
    private UUID owner;
    private Location spawnLocation;
    private Location corner1;
    private Location corner2;
    private CuboidRegion mineCuboidRegion;
    private IWrappedRegion mineRegion;
    private int mineSize;
    private boolean isPublic;
    private double tax;
    private Material material;
    private List<UUID> playersInMine;
    private List<UUID> whitelistedPlayers = new ArrayList<>();
    private List<UUID> bannedPlayers = new ArrayList<>();
    private Task resetTask;
    private MineData mineData;

    public World getWorld() {
        return world;
    }

    public PrivateMines getPrivateMines() {
        return privateMines;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }

    public CuboidRegion getMineCuboidRegion() {
        return mineCuboidRegion;
    }

    public void setMineCuboidRegion(CuboidRegion mineCuboidRegion) {
        this.mineCuboidRegion = mineCuboidRegion;
    }

    public IWrappedRegion getMineRegion() {
        return mineRegion;
    }

    public void setMineRegion(IWrappedRegion mineRegion) {
        this.mineRegion = mineRegion;
    }

    public int getMineSize() {
        return mineSize;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public MineData getMineData() {
        return mineData;
    }

    public void setMineData(MineData mineData) {
        this.mineData = mineData;
    }

    public Mine(PrivateMines privateMines) {
        this.privateMines = privateMines;
    }

    public void load() {
    }

    public void whitelistPlayer(OfflinePlayer player) {
        if (this.whitelistedPlayers.contains(player.getUniqueId())) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cThat player is already whitelisted!");
        } else if (whitelistedPlayers.size() <= MAX_WHITELISTED_PLAYERS) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) Max whitelisted players reached! (9)");
        } else {
            this.whitelistedPlayers.add(player.getUniqueId());
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&aYou added &6%player% &ato your mine."
                    .replaceAll("%player%", player.getName()));
        }
    }

    public void teleport(Player player) {
        if (this.whitelistedPlayers.contains(player.getUniqueId()) || player.getUniqueId().equals(this.owner)
                && !(this.bannedPlayers.contains(player.getUniqueId()))) {
            player.teleport(this.spawnLocation);
        }
    }

    public void startAutoResetTask() {
        this.resetTask = Schedulers.async().runRepeating(this::reset, 30L, TimeUnit.SECONDS, AUTO_RESET_INTERVAL, TimeUnit.SECONDS);
    }

    public void cancelResetTask() {
        this.resetTask.close();
    }

    public void reset() {
        playersInMine.forEach((p) -> Players.getNullable(p).teleport(spawnLocation));
        com.sk89q.worldedit.world.World world = new BukkitWorld(this.world);
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
