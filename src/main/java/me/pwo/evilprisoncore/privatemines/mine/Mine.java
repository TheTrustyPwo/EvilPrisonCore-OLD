package me.pwo.evilprisoncore.privatemines.mine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.ranks.Ranks;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Mine {
    private static final int autoResetInterval = 300;
    private static final double resetPercentage = 30.0D;
    private final UUID owner;
    private World world;
    private Location spawnLocation;
    private Location corner1;
    private Location corner2;
    private int mineSize;
    private boolean isPublic;
    private double tax;
    private Material material;
    private List<UUID> playersInMine;
    private List<UUID> playersWithAccess;
    private Task resetTask;

    public Mine(UUID owner, boolean isPublic, double tax, Material material, List<UUID> playersWithAccess) {
        this.owner = owner;
        this.isPublic = isPublic;
        this.tax = tax;
        this.material = material;
        this.playersWithAccess = playersWithAccess;
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

    public void build() {

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
