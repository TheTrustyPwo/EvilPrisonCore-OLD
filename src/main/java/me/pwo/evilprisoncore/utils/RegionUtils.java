package me.pwo.evilprisoncore.utils;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Set;
import java.util.stream.Collectors;

public class RegionUtils {
    public static IWrappedRegion getMineRegionWithHighestPriority(Location paramLocation) {
        Set<IWrappedRegion> set = WorldGuardWrapper.getInstance().getRegions(paramLocation).stream().filter(paramIWrappedRegion -> paramIWrappedRegion.getId().startsWith("mine")).collect(Collectors.toSet());
        IWrappedRegion iWrappedRegion = null;
        for (IWrappedRegion iWrappedRegion1 : set) {
            if (iWrappedRegion == null || iWrappedRegion1.getPriority() > iWrappedRegion.getPriority())
                iWrappedRegion = iWrappedRegion1;
        }
        return iWrappedRegion;
    }

    public static IWrappedRegion getRegionWithHighestPriority(Location paramLocation) {
        Set<IWrappedRegion> set = WorldGuardWrapper.getInstance().getRegions(paramLocation);
        IWrappedRegion iWrappedRegion = null;
        for (IWrappedRegion iWrappedRegion1 : set) {
            if (iWrappedRegion == null || iWrappedRegion1.getPriority() > iWrappedRegion.getPriority())
                iWrappedRegion = iWrappedRegion1;
        }
        return iWrappedRegion;
    }

    public static IWrappedRegion getFirstRegionAtLocation(Location paramLocation) {
        Set<IWrappedRegion> set = WorldGuardWrapper.getInstance().getRegions(paramLocation);
        return (set.size() == 0) ? null : set.iterator().next();
    }
}
