package me.pwo.evilprisoncore.privatemines.worldedit;

public class WorldEditVector {
    private final double x;

    private final double y;

    private final double z;

    public WorldEditVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public WorldEditVector copy() {
        return new WorldEditVector(this.x, this.y, this.z);
    }

    public String toString() {
        return "WorldEditVector{x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
    }
}
