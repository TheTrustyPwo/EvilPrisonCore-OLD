package me.pwo.evilprisoncore.privatemines.mine.data;

import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.*;

public class MineData {
    UUID mineOwner;
    double spawnX;
    double spawnY;
    double spawnZ;
    int mineMinX;
    int mineMinY;
    int mineMinZ;
    int mineMaxX;
    int mineMaxY;
    int mineMaxZ;
    int regionMinX;
    int regionMinY;
    int regionMinZ;
    int regionMaxX;
    int regionMaxY;
    int regionMaxZ;
    String material;
    boolean isPublic;
    double tax;

    public double getSpawnX() {
        return spawnX;
    }

    public double getSpawnY() {
        return spawnY;
    }

    public double getSpawnZ() {
        return spawnZ;
    }

    int size;
    List<UUID> whitelistedPlayers = new ArrayList<>();
    List<UUID> bannedPlayers = new ArrayList<>();

    public UUID getMineOwner() {
        return mineOwner;
    }

    public void setMineOwner(UUID mineOwner) {
        this.mineOwner = mineOwner;
    }

    public void setSpawnX(double spawnX) {
        this.spawnX = spawnX;
    }

    public void setSpawnY(double spawnY) {
        this.spawnY = spawnY;
    }

    public void setSpawnZ(double spawnZ) {
        this.spawnZ = spawnZ;
    }

    public int getMineMinX() {
        return mineMinX;
    }

    public void setMineMinX(int mineMinX) {
        this.mineMinX = mineMinX;
    }

    public int getMineMinY() {
        return mineMinY;
    }

    public void setMineMinY(int mineMinY) {
        this.mineMinY = mineMinY;
    }

    public int getMineMinZ() {
        return mineMinZ;
    }

    public void setMineMinZ(int mineMinZ) {
        this.mineMinZ = mineMinZ;
    }

    public int getMineMaxX() {
        return mineMaxX;
    }

    public void setMineMaxX(int mineMaxX) {
        this.mineMaxX = mineMaxX;
    }

    public int getMineMaxY() {
        return mineMaxY;
    }

    public void setMineMaxY(int mineMaxY) {
        this.mineMaxY = mineMaxY;
    }

    public int getMineMaxZ() {
        return mineMaxZ;
    }

    public void setMineMaxZ(int mineMaxZ) {
        this.mineMaxZ = mineMaxZ;
    }

    public int getRegionMinX() {
        return regionMinX;
    }

    public void setRegionMinX(int regionMinX) {
        this.regionMinX = regionMinX;
    }

    public int getRegionMinY() {
        return regionMinY;
    }

    public void setRegionMinY(int regionMinY) {
        this.regionMinY = regionMinY;
    }

    public int getRegionMinZ() {
        return regionMinZ;
    }

    public void setRegionMinZ(int regionMinZ) {
        this.regionMinZ = regionMinZ;
    }

    public int getRegionMaxX() {
        return regionMaxX;
    }

    public void setRegionMaxX(int regionMaxX) {
        this.regionMaxX = regionMaxX;
    }

    public int getRegionMaxY() {
        return regionMaxY;
    }

    public void setRegionMaxY(int regionMaxY) {
        this.regionMaxY = regionMaxY;
    }

    public int getRegionMaxZ() {
        return regionMaxZ;
    }

    public void setRegionMaxZ(int regionMaxZ) {
        this.regionMaxZ = regionMaxZ;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<UUID> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void whitelistPlayer(UUID uuid) {
        this.whitelistedPlayers.add(uuid);
    }

    public void unwhitelistPlayer(UUID uuid) {
        this.whitelistedPlayers.remove(uuid);
    }

    public List<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public void banPlayer(UUID uuid) {
        this.bannedPlayers.add(uuid);
    }

    public void unbanPlayer(UUID uuid) {
        this.bannedPlayers.remove(uuid);
    }
}
