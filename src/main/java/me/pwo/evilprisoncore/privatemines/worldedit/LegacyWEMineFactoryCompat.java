package me.pwo.evilprisoncore.privatemines.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.object.visitor.FastIterator;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;
import me.pwo.evilprisoncore.privatemines.manager.PrivateMinesWorldManager;
import org.bukkit.Location;

import java.util.Iterator;

public class LegacyWEMineFactoryCompat implements MineFactoryCompat<Schematic> {
    private final EditSession editSession;

    private final World world;

    public LegacyWEMineFactoryCompat(PrivateMinesWorldManager manager) {
        this.world = FaweAPI.getWorld(manager.getMinesWorld().getName());
        this
                .editSession = (new EditSessionBuilder(this.world)).allowedRegionsEverywhere().limitUnlimited().fastmode(Boolean.TRUE).build();
    }

    public WorldEditRegion pasteSchematic(Schematic schematic, Location location) {
        Clipboard clipboard = schematic.getClipboard();
        if (clipboard == null)
            throw new IllegalStateException("Schematic does not have a Clipboard! This should never happen!");
        location.setY(clipboard.getOrigin().getBlockY());
        Vector centerVector = BukkitUtil.toVector(location);
        schematic.paste((World)this.editSession, centerVector, false, true, null);
        Region region = clipboard.getRegion();
        region.setWorld(this.world);
        try {
            region.shift(centerVector.subtract(clipboard.getOrigin()));
        } catch (RegionOperationException e) {
            e.printStackTrace();
        }
        WorldEditVector min = LegacyWEHook.transform(region.getMinimumPoint());
        WorldEditVector max = LegacyWEHook.transform(region.getMaximumPoint());
        return new WorldEditRegion(min, max, location.getWorld());
    }

    public Iterable<WorldEditVector> loop(WorldEditRegion region) {
        FastIterator vectors = new FastIterator(LegacyWEHook.transform(region), this.editSession);
        final Iterator<Vector> fastIterator = vectors.iterator();
        Iterator<WorldEditVector> weVecIterator = new Iterator<WorldEditVector>() {
            public boolean hasNext() {
                return fastIterator.hasNext();
            }

            public WorldEditVector next() {
                return LegacyWEHook.transform(fastIterator.next());
            }
        };
        return () -> weVecIterator;
    }
}
