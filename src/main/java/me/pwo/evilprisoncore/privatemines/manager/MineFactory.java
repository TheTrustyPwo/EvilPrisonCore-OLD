package me.pwo.evilprisoncore.privatemines.manager;

import me.pwo.evilprisoncore.privatemines.PrivateMines;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.privatemines.worldedit.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Directional;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class MineFactory<M extends MineSchematic<S>, S> {
    protected final PrivateMines privateMines;
    private final MineFactoryCompat<S> compat;

    public MineFactory(PrivateMines privateMines, MineFactoryCompat<S> compat) {
        this.privateMines = privateMines;
        this.compat = compat;
    }

    public Mine create(Player player, Location location) {
        WorldEditRegion region = this.compat.pasteSchematic();
        Location spawnLocation = null;
        WorldEditVector min = null;
        WorldEditVector max = null;
        World world = location.getWorld();
        for (WorldEditVector vector : this.compat.loop(region)) {
            Block blockAt = world.getBlockAt((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
            Material material = blockAt.getType();
            if (material == Material.AIR || material.name().equals("LEGACY_AIR")) continue;
            if (spawnLocation == null && material == Material.CHEST) {
                spawnLocation = new Location(location.getWorld(), vector.getX() + 0.5D, vector.getY() + 0.5D, vector.getZ() + 0.5D);
                Block block = spawnLocation.getBlock();
                if (block.getState().getData() instanceof Directional)
                    spawnLocation.setYaw(WorldEditUtil.getYaw(((Directional) block.getState().getData()).getFacing()));
                blockAt.setType(Material.AIR);
            } else if (material == Material.POWERED_RAIL) {
                if (min == null) min = vector.copy();
                else if (max == null) max = vector.copy();
            }
        }
        if (min == null || max == null || min.equals(max))
            throw new IllegalArgumentException("Mine schematic did not define 2 distinct corner blocks, mine cannot be formed");
        if (spawnLocation == null) spawnLocation = location.getWorld().getHighestBlockAt(location).getLocation();
        WorldEditRegion mainRegion = new WorldEditRegion(region.getMinimumPoint(), region.getMaximumPoint(), location.getWorld());
        IWrappedRegion worldGuardRegion = createMainWorldGuardRegion(player, mainRegion);
        WorldEditRegion miningRegion = new WorldEditRegion(min, max, location.getWorld());
        IWrappedRegion mineRegion = createMineWorldGuardRegion(player, miningRegion, worldGuardRegion);
        MineLocations locations = new MineLocations(spawnLocation, min, max, mineRegion);
        Mine mine = new Mine();
        mine.reset();
        return mine;
    }

    @NotNull
    protected IWrappedRegion createMainWorldGuardRegion(Player owner, WorldEditRegion r) {
        IWrappedRegion region = WorldGuardWrapper.getInstance().addCuboidRegion(
                owner.getUniqueId().toString(),
                r.getMinimumLocation(),
                r.getMaximumLocation())
                .orElseThrow(() -> new RuntimeException("Could not create Main WorldGuard region"));
        region.getOwners().addPlayer(owner.getUniqueId());
        setMainFlags(region);
        return region;
    }

    @NotNull
    protected IWrappedRegion createMineWorldGuardRegion(Player owner, WorldEditRegion region, IWrappedRegion parent) {
        IWrappedRegion mineRegion = WorldGuardWrapper.getInstance().addCuboidRegion(
                "mine-" + owner.getUniqueId().toString(),
                region.getMinimumLocation(),
                region.getMaximumLocation())
                .orElseThrow(() -> new RuntimeException("Could not create Mine WorldGuard region"));
        parent.getOwners().getPlayers().forEach(uuid -> mineRegion.getOwners().addPlayer(uuid));
        mineRegion.setPriority(1);
        setMineFlags(mineRegion);
        return mineRegion;
    }

    public void setMineFlags(IWrappedRegion region) {
        WorldGuardWrapper.getInstance().getFlag("block-break", WrappedState.class)
                .ifPresent(flag -> region.setFlag(flag, WrappedState.ALLOW));
    }

    public void setMainFlags(IWrappedRegion region) {
        WorldGuardWrapper w = WorldGuardWrapper.getInstance();
        Stream.of(w.getFlag("block-place", WrappedState.class),
                w.getFlag("block-break", WrappedState.class),
                w.getFlag("mob-spawning", WrappedState.class))
                .filter(Optional::isPresent).map(Optional::get).forEach((flag) -> region.setFlag(flag, WrappedState.ALLOW));
    }
}
