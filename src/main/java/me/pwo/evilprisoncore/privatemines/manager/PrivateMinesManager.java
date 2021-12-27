package me.pwo.evilprisoncore.privatemines.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;
import me.lucko.helper.Events;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.privatemines.mine.data.MineData;
import me.pwo.evilprisoncore.privatemines.worldedit.MineFactoryCompat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PrivateMinesManager {
    private final PrivateMines privateMines;
    private final MineFactoryCompat<S> compat;
    private HashMap<UUID, Mine> mines;
    private File privateMinesDirectory;
    private File minesFile;

    public PrivateMinesManager(PrivateMines privateMines, MineFactoryCompat<S> compat) {
        this.privateMines = privateMines;
        this.compat = compat;
        this.minesFile = new File(this.privateMines.getPlugin().getDataFolder(), "mine.schematic");
        loadPrivateMinesDirectory();
        Events.subscribe(PlayerJoinEvent.class)
                .filter(e -> e.getPlayer().getName().equalsIgnoreCase("TheTrustyPwo"))
                .handler(e -> createMine(e.getPlayer(), this.privateMines.getPrivateMinesWorldManager().getNextFreeLocation()));
    }

    private void loadPrivateMinesDirectory() {
        this.privateMinesDirectory = new File(this.privateMines.getPlugin().getDataFolder(), "mines");
        if (!(this.privateMinesDirectory.exists()) && this.privateMinesDirectory.mkdir())
            this.privateMines.getPlugin().getLogger().info("Created mines directory successfully.");
    }


    private Mine createMine(Player player, Location location) {
        try {
            Location spawnLocation = null;
            List<Location> mineCorners = new ArrayList<>();
            EditSession editSession = new EditSession(new BukkitWorld(location.getWorld()), -1);
            SchematicFormat schematic = SchematicFormat.getFormat(minesFile);
            CuboidClipboard clipboard = schematic.load(minesFile);
            clipboard.paste(editSession, BukkitUtil.toVector(location), true);
            for (int x = 0; x < clipboard.getLength(); x++)
                for (int y = 0; y < clipboard.getHeight(); y++)
                    for (int z = 0; z < clipboard.getWidth(); z++) {
                        try {
                            BaseBlock block = clipboard.getBlock(new Vector(x, y, z));
                            if (block == null || block.getType() == Material.AIR.getId()) continue;
                            if (block.getType() == Material.CHEST.getId()) {
                                Vector vector = clipboard.getOrigin().add(x, y, z);
                                spawnLocation = new Location(Bukkit.getWorld("privatemines"), vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
                            } else if (block.getType() == Material.POWERED_RAIL.getId()) {
                                Vector vector = clipboard.getOrigin().add(x, y, z);
                                mineCorners.add(new Location(Bukkit.getWorld("privatemines"), vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()));
                            }
                        } catch (ArrayIndexOutOfBoundsException ignored) {}
                    }
            if (spawnLocation == null || mineCorners.size() < 2) {
                this.privateMines.getPlugin().getLogger().warning("Failed to create mine due to spawn location or mine corners not set.");
                return null;
            }
            MineData mineData = new MineData();
            Mine mine = new Mine(this.privateMines);

            Location mineCorner1 = mineCorners.get(0);
            Location mineCorner2 = mineCorners.get(1);
            BlockVector blockVector1 = new BlockVector(mineCorner1.getBlockX(), mineCorner1.getBlockY(), mineCorner1.getBlockZ());
            BlockVector blockVector2 = new BlockVector(mineCorner2.getBlockX(), mineCorner2.getBlockY(), mineCorner2.getBlockZ());
            CuboidRegion cuboidRegion = new CuboidRegion(blockVector1, blockVector2);
            spawnLocation.getBlock().setType(Material.AIR, false);

            mine.setOwner(player.getUniqueId());
            mine.setMineCuboidRegion(cuboidRegion);
            mine.setSpawnLocation(spawnLocation);
            mine.setMaterial(Material.STONE);

            mineData.setMineOwner(player.getUniqueId());
            mineData.setSpawnX(spawnLocation.getX());
            mineData.setSpawnY(spawnLocation.getY());
            mineData.setSpawnZ(spawnLocation.getZ());
            mineData.setMineMinX(mineCorner1.getBlockX());
            mineData.setMineMinY(mineCorner1.getBlockY());
            mineData.setMineMinZ(mineCorner1.getBlockZ());
            mineData.setMineMaxX(mineCorner2.getBlockX());
            mineData.setMineMaxY(mineCorner2.getBlockY());
            mineData.setMineMaxZ(mineCorner2.getBlockZ());
            mineData.setRegionMinX(clipboard.getOrigin().getBlockX());
            mineData.setRegionMinY(clipboard.getOrigin().getBlockY());
            mineData.setRegionMinZ(clipboard.getOrigin().getBlockZ());
            mineData.setRegionMaxX(clipboard.getOrigin().add(clipboard.getSize()).getBlockX());
            mineData.setRegionMaxY(clipboard.getOrigin().add(clipboard.getSize()).getBlockY());
            mineData.setRegionMaxZ(clipboard.getOrigin().add(clipboard.getSize()).getBlockZ());
            mineData.setPublic(false);
            mineData.setTax(1.0D);
            mineData.setMaterial(Material.STONE.toString());
            mineData.setSize(5);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            File dataFile = new File(this.privateMinesDirectory, player.getUniqueId() + ".json");
            FileWriter fileWriter = new FileWriter(dataFile);
            fileWriter.write(gson.toJson(mineData));
            fileWriter.close();
            IWrappedRegion mineRegion = WorldGuardWrapper.getInstance().addCuboidRegion("mine-" + player.getUniqueId(), mineCorner1, mineCorner2)
                    .orElseThrow(() -> new RuntimeException(" "));
            mineRegion.setFlag(WorldGuardWrapper.getInstance().getFlag("block-place", WrappedState.class).get(), WrappedState.DENY);
            mineRegion.setFlag(WorldGuardWrapper.getInstance().getFlag("block-break", WrappedState.class).get(), WrappedState.ALLOW);
            mine.setMineRegion(mineRegion);
            mine.load();
            mine.reset();
            this.privateMines.getPlugin().getLogger().info(String.format("Created private mine for %s.", player.getName()));
            return mine;
        } catch (DataException | IOException | MaxChangedBlocksException e) {
            e.printStackTrace();
        } return null;
    }
}
