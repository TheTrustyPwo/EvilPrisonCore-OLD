package me.pwo.evilprisoncore.privatemines.mine;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class Mine {
    private static final int AUTO_RESET_INTERVAL = 300;
    private static final double AUTO_RESET_PERCENTAGE = 30.0D;
    private static final int MAX_WHITELISTED_PLAYERS = 9;
    private static final int MINE_DEPTH = 64;
    private static final int MAX_MINE_SIZE = 149;
    private final World world = PrivateMines.getInstance().getPrivateMinesWorldManager().getMinesWorld();
    private final UUID owner;
    private final Location spawnLocation;
    private final Location center;
    private final CuboidRegion mainRegion;
    private int mineSize;
    private boolean isPublic;
    private double tax;
    private Material material;
    private final Set<UUID> whitelistedPlayers;
    private final Set<UUID> bannedPlayers;
    private CuboidRegion mineRegion;
    private Task resetTask;

    public UUID getOwner() {
        return owner;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Location getCenter() {
        return center;
    }

    public CuboidRegion getMainRegion() {
        return mainRegion;
    }

    public CuboidRegion getMineRegion() {
        return mineRegion;
    }

    public int getMineSize() {
        return mineSize;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public double getTax() {
        return tax;
    }

    public Material getMaterial() {
        return material;
    }

    public Set<UUID> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        OfflinePlayer player = Players.getOfflineNullable(this.owner);
        if (player != null && player.isOnline())
            PlayerUtils.sendMessage(player.getPlayer(), "&eYou have %toggle% &epublic access for your mine."
                    .replaceAll("%toggle%", isPublic ? "&a&lENABLED" : "&c&lDISABLED"));
    }

    public void setMineSize(int mineSize) {
        int currentSize = this.mineSize;
        this.mineSize = Math.min(mineSize, MAX_MINE_SIZE);
        reloadMineRegion(mineSize < currentSize);
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Mine(UUID owner, Location spawnLocation, Location center, CuboidRegion mainRegion, int mineSize,
                boolean isPublic, Material material, double tax, Set<UUID> bannedPlayers, Set<UUID> whitelistedPlayers) {
        this.owner = owner;
        this.spawnLocation = spawnLocation;
        this.center = center;
        this.mainRegion = mainRegion;
        this.mineSize = mineSize;
        this.isPublic = isPublic;
        this.material = material;
        this.tax = tax;
        this.bannedPlayers = bannedPlayers;
        this.whitelistedPlayers = whitelistedPlayers;
        reloadMineRegion(false);
        startAutoResetTask();
    }

    private void reloadMineRegion(boolean regenerate) {
        int radius = (mineSize - 1) / 2;
        this.mineRegion = new CuboidRegion(new BukkitWorld(this.world),
                WorldEditUtil.toWEVector(center).add(radius, 0, radius),
                WorldEditUtil.toWEVector(center).add(-1 * radius, -1 * MINE_DEPTH, -1 * radius));
        IWrappedRegion worldGuardMineRegion = WorldGuardWrapper.getInstance().addCuboidRegion(
                        "mine-" + this.owner,
                        WorldEditUtil.toLocation(mineRegion.getMinimumPoint(), world),
                        WorldEditUtil.toLocation(mineRegion.getMaximumPoint(), world))
                .orElseThrow(() -> new RuntimeException("Could not create Mine WorldGuard region"));
        worldGuardMineRegion.setPriority(1);
        WorldGuardWrapper.getInstance().getFlag("block-break", WrappedState.class)
                .ifPresent(flag -> worldGuardMineRegion.setFlag(flag, WrappedState.ALLOW));
        try {
            if (regenerate) {
                int rad = (MAX_MINE_SIZE - 1) / 2;
                EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(this.world.getName()))).fastmode(true).build();
                session.setBlocks(new CuboidRegion(WorldEditUtil.toWEVector(center).add(rad, 0, rad),
                        WorldEditUtil.toWEVector(center).add(-1 * rad, -1 * MINE_DEPTH, -1 * rad)),
                        new BaseBlock(43, 8));
                session.flushQueue();
            }
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        } reset();
    }

    public void whitelist(@Nullable OfflinePlayer player) {
        if (player == null) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cInvalid Player");
            return;
        }
        if (this.whitelistedPlayers.contains(player.getUniqueId())) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cThat player is already whitelisted!");
        } else if (player.getUniqueId().equals(this.owner)) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cYou cannot whitelist the owner!");
        } else if (whitelistedPlayers.size() >= MAX_WHITELISTED_PLAYERS) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cMax whitelisted players reached! &4(9)");
        } else {
            this.whitelistedPlayers.add(player.getUniqueId());
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&aYou whitelisted &6%player% &ato your mine."
                    .replaceAll("%player%", player.getName()));
        }
    }

    public void unwhitelist(@Nullable OfflinePlayer player) {
        if (player == null) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cInvalid Player");
            return;
        }
        this.whitelistedPlayers.remove(player.getUniqueId());
        PlayerUtils.sendMessage(Players.getNullable(this.owner), "&aYou unwhitelisted &6%player% &afrom your mine."
                .replaceAll("%player%", player.getName()));
    }

    public void ban(@Nullable OfflinePlayer player) {
        if (player == null) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cInvalid Player");
            return;
        }
        if (this.bannedPlayers.contains(player.getUniqueId())) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cThat player is already banned!");
        } else if (player.getUniqueId().equals(this.owner)) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cYou cannot ban the owner!");
        } else if (this.whitelistedPlayers.contains(player.getUniqueId())) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) Unwhitelist the player first!");
        } else {
            this.bannedPlayers.add(player.getUniqueId());
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&aYou banned &6%player% &afrom your mine."
                    .replaceAll("%player%", player.getName()));
        }
    }

    public void unban(@Nullable OfflinePlayer player) {
        if (player == null) {
            PlayerUtils.sendMessage(Players.getNullable(this.owner), "&c&l(!) &cInvalid Player");
            return;
        }
        this.bannedPlayers.remove(player.getUniqueId());
        PlayerUtils.sendMessage(Players.getNullable(this.owner), "&aYou unbanned &6%player% &afrom your mine."
                .replaceAll("%player%", player.getName()));
    }

    public void teleport(Player player) {
        if (this.whitelistedPlayers.contains(player.getUniqueId()) || player.getUniqueId().equals(this.owner)
                && !(this.bannedPlayers.contains(player.getUniqueId()))) {
            player.teleport(this.spawnLocation);
        }
    }

    public void startAutoResetTask() {
        this.resetTask = Schedulers.async().runRepeating(this::reset, 0L, TimeUnit.SECONDS, AUTO_RESET_INTERVAL, TimeUnit.SECONDS);
    }

    public List<Player> getPlayersInMine() {
        return Players.all().stream()
                .filter(player -> this.mainRegion.contains(WorldEditUtil.toWEVector(player.getLocation())))
                .filter(player -> this.mainRegion.getWorld().getName().equals(player.getWorld().getName())).collect(Collectors.toList());
    }

    public void reset() {
        if (getPlayersInMine().size() == 0 && !(Players.getOfflineNullable(this.owner).isOnline())) return;
        long l1 = Time.nowMillis();
        try {
            EditSession session = (new EditSessionBuilder(FaweAPI.getWorld(this.world.getName()))).fastmode(true).build();
            session.setBlocks(this.mineRegion, new BaseBlock(this.material.getId()));
            session.flushQueue();
            long l2 = Time.nowMillis();
            PrivateMines.getInstance().getPlugin().getLogger().info("Reset " + this.mineSize + "x" + this.mineSize + " mine in " + (l2-l1) + "ms!");
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        Players.all().stream()
                .filter(player -> this.mineRegion.contains(WorldEditUtil.toWEVector(player.getLocation())))
                .filter(player -> this.mineRegion.getWorld().getName().equals(player.getWorld().getName()))
                .forEach(player -> {
                    teleport(player);
                    PlayerUtils.sendMessage(player, "&a%player%'s mine has reset!".replaceAll("%player%", Players.getOfflineNullable(this.owner).getName()));
                });
    }
}
