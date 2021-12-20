package me.pwo.evilprisoncore.autosell;

import org.bukkit.Location;
import org.bukkit.Material;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Set;

public class SellRegion {
    private final IWrappedRegion region;
    private final String permissionRequired;
    private final Map<Material, Double> sellPrices;

    @ConstructorProperties({"region", "permissionRequired", "sellPrices"})
    public SellRegion(IWrappedRegion paramIWrappedRegion, String paramString, Map<Material, Double> paramMap) {
        this.region = paramIWrappedRegion;
        this.permissionRequired = paramString;
        this.sellPrices = paramMap;
    }

    public IWrappedRegion getRegion() {
        return this.region;
    }

    public String getPermissionRequired() {
        return this.permissionRequired;
    }

    public double getSellPriceFor(Material paramCompMaterial) {
        return this.sellPrices.getOrDefault(paramCompMaterial, 0.0D);
    }

    public boolean sellsMaterial(Material paramCompMaterial) {
        return this.sellPrices.containsKey(paramCompMaterial);
    }

    public void addSellPrice(Material paramCompMaterial, double paramDouble) {
        this.sellPrices.put(paramCompMaterial, paramDouble);
    }

    public Set<Material> getSellingMaterials() {
        return this.sellPrices.keySet();
    }

    public boolean contains(Location paramLocation) {
        return this.region.contains(paramLocation);
    }
}
