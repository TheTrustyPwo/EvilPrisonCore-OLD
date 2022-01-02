package me.pwo.evilprisoncore.privatemines.manager;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class PrivateMinesManager {
    private static final Pattern dataFilePattern = Pattern.compile("(.*?)\\.(json)");
    private final Gson gson;
    private final PrivateMines privateMines;
    private final World world;
    private final Map<UUID, Mine> mines = new HashMap<>();
    private File privateMinesDirectory;
    private Clipboard mineSchematic;

    public File getPrivateMinesDirectory() {
        return privateMinesDirectory;
    }

    public PrivateMinesManager(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.world = this.privateMines.getPrivateMinesWorldManager().getMinesWorld();
        loadMineSchematic();
        this.gson = new Gson();
        loadPrivateMinesDirectory();
        loadAllMines();
        Schedulers.async().runRepeating(this::saveAllMines, 10L, TimeUnit.MINUTES, 10L, TimeUnit.MINUTES);
    }

    private void loadMineSchematic() {
        try {
            File minesFile = new File(this.privateMines.getPlugin().getDataFolder(), "mine.schematic");
            ClipboardReader reader = ClipboardFormat.findByFile(minesFile).getReader(new FileInputStream(minesFile));
            this.mineSchematic = reader.read((new BukkitWorld(this.world)).getWorldData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPrivateMinesDirectory() {
        this.privateMinesDirectory = new File(this.privateMines.getPlugin().getDataFolder(), "mines");
        if (!(this.privateMinesDirectory.exists()) && this.privateMinesDirectory.mkdir())
            this.privateMines.getPlugin().getLogger().info("Created mines directory successfully.");
    }

    private void loadAllMines() {
        File[] files = this.privateMinesDirectory.listFiles();
        if (files == null) return;
        Arrays.stream(files).forEach(file -> {
            if (file.getName().matches(String.valueOf(dataFilePattern))) {
                try {
                    BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Mine mine = deserialize(this.gson.fromJson(bufferedReader, type));
                    this.mines.put(mine.getOwner(), mine);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        this.privateMines.getPlugin().getLogger().info(String.format("Loaded mines for %,d players.", this.mines.size()));
    }

    public void saveAllMines() {
        this.mines.forEach((owner, mine) -> {
            try {
                File file = new File(getPrivateMinesDirectory(), owner.toString() + ".json");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(this.gson.toJson(serialize(mine)));
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.privateMines.getPlugin().getLogger().info("Saved all mines data.");
    }

    public void savePlayerMine(OfflinePlayer player) {
        Mine mine = this.mines.get(player.getUniqueId());
        if (mine == null) return;
        try {
            File file = new File(getPrivateMinesDirectory(), player.getUniqueId().toString() + ".json");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(this.gson.toJson(serialize(mine)));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> serialize(Mine mine) {
        Map<String, Object> map = new TreeMap<>();
        map.put("Owner", mine.getOwner());
        map.put("Public", mine.isPublic());
        map.put("Material", mine.getMaterial().name());
        map.put("Spawn", mine.getSpawnLocation().serialize());
        map.put("Center", mine.getCenter().serialize());
        map.put("MainCorner1", WorldEditUtil.toBukkitVector(mine.getMainRegion().getMinimumPoint()).serialize());
        map.put("MainCorner2", WorldEditUtil.toBukkitVector(mine.getMainRegion().getMaximumPoint()).serialize());
        map.put("Size", mine.getMineSize());
        map.put("Tax", mine.getTax());
        map.put("BannedPlayers", mine.getBannedPlayers());
        map.put("WhitelistedPlayers", mine.getWhitelistedPlayers());
        return map;
    }

    private Mine deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString(map.get("Owner").toString());
        boolean isPublic = (boolean) map.get("Public");
        Material material = Material.getMaterial((String) map.get("Material"));
        Location spawnLocation = Location.deserialize((Map<String, Object>) map.get("Spawn"));
        Location center = Location.deserialize((Map<String, Object>) map.get("Center"));
        org.bukkit.util.Vector mainCorner1 = org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("MainCorner1"));
        org.bukkit.util.Vector mainCorner2 = org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("MainCorner2"));
        int size = gson.fromJson(map.get("Size").toString(), int.class);
        double tax = gson.fromJson(map.get("Tax").toString(), double.class);
        Type setType = new TypeToken<HashSet<String>>(){}.getType();
        Set<UUID> bannedPlayers = new HashSet<>(9);
        Set<UUID> whitelistedPlayers = new HashSet<>(9);
        ((Set<String>) gson.fromJson(map.get("BannedPlayers").toString(), setType)).forEach(string -> bannedPlayers.add(UUID.fromString(string)));
        ((Set<String>) gson.fromJson(map.get("WhitelistedPlayers").toString(), setType)).forEach(string -> bannedPlayers.add(UUID.fromString(string)));
        return new Mine(owner, spawnLocation, center,
                new CuboidRegion(new BukkitWorld(this.world), WorldEditUtil.toWEVector(mainCorner1), WorldEditUtil.toWEVector(mainCorner2))
                ,size, isPublic, material, tax, bannedPlayers, whitelistedPlayers);
    }

    public Mine getPlayerMine(UUID uuid) {
        return this.mines.get(uuid);
    }

    public Map<UUID, Mine> getAllMines() {
        return mines;
    }

    public void createMine(Player player) {
        createMine(player, this.privateMines.getPrivateMinesWorldManager().getNextFreeLocation());
    }

    public Mine createMine(Player player, Location location) {
        try {
            WorldData worldData = (new BukkitWorld(this.world)).getWorldData();
            EditSession editSession = (new EditSessionBuilder(FaweAPI.getWorld(this.world.getName()))).fastmode(true).build();
            Vector vector = BukkitUtil.toVector(location);
            Clipboard clipboard = this.mineSchematic;
            Operation operation = new ClipboardHolder(clipboard, worldData)
                    .createPaste(editSession, worldData).to(vector).ignoreAirBlocks(true).build();
            Operations.complete(operation);
            editSession.flushQueue();
            Region clipboardRegion = clipboard.getRegion();
            clipboardRegion.shift(vector.subtract(clipboard.getOrigin()));
            Location spawnLocation = null;
            Location center = null;
            int tries = 0;
            while ((spawnLocation == null || center == null) && tries <= 3) {
                for (BlockVector blockVector : clipboardRegion) {
                    if (spawnLocation != null && center != null) break;
                    Block block = world.getBlockAt(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ());
                    Material material = block.getType();
                    if (material == Material.AIR) continue;
                    if (spawnLocation == null && material == Material.CHEST) {
                        spawnLocation = new Location(world, blockVector.getBlockX() + 0.5D, blockVector.getBlockY() + 0.5D, blockVector.getBlockZ() + 0.5D);
                        block.setType(Material.AIR);
                    } else if (material == Material.FURNACE) center = WorldEditUtil.toLocation(blockVector, world);
                }
                if (spawnLocation == null || center == null) {
                    this.privateMines.getPlugin().getLogger().warning(Text.colorize("&cCould not find SPAWN or CENTER location! Retrying... (%tries%/3)"
                            .replaceAll("%tries%", String.valueOf(tries))));
                    tries++;
                }
            }
            CuboidRegion mainRegion = new CuboidRegion(new BukkitWorld(this.world),
                    clipboardRegion.getMinimumPoint().setY(0), clipboardRegion.getMaximumPoint().setY(this.world.getMaxHeight()));
            IWrappedRegion worldGuardMainRegion = WorldGuardWrapper.getInstance().addCuboidRegion(
                            player.getUniqueId().toString(),
                            WorldEditUtil.toLocation(mainRegion.getMinimumPoint(), world),
                            WorldEditUtil.toLocation(mainRegion.getMaximumPoint(), world))
                    .orElseThrow(() -> new RuntimeException("Could not create Main WorldGuard region"));
            worldGuardMainRegion.getOwners().addPlayer(player.getUniqueId());
            WorldGuardWrapper w = WorldGuardWrapper.getInstance();
            Stream.of(w.getFlag("block-place", WrappedState.class),
                            w.getFlag("block-break", WrappedState.class))
                    .filter(Optional::isPresent).map(Optional::get).forEach((flag) -> worldGuardMainRegion.setFlag(flag, WrappedState.ALLOW));
            Mine mine = new Mine(player.getUniqueId(), spawnLocation, center, mainRegion, 11, false, Material.STONE,
                    1.0D, new HashSet<>(), new HashSet<>());
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                File file = new File(getPrivateMinesDirectory(),
                        player.getUniqueId() + ".json");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(gson.toJson(serialize(mine)));
                fileWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            this.mines.put(player.getUniqueId(), mine);
            return mine;
        } catch (WorldEditException ioException) {
            ioException.printStackTrace();
        } return null;
    }
}
