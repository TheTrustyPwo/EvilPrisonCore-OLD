package me.pwo.evilprisoncore.utils;

import org.bukkit.Location;

public enum Direction {
    NORTH(0.0D, -1.0D),
    NORTH_EAST(1.0D, -1.0D),
    EAST(1.0D, 0.0D),
    SOUTH_EAST(1.0D, 1.0D),
    SOUTH(0.0D, 1.0D),
    SOUTH_WEST(-1.0D, 1.0D),
    WEST(-1.0D, 0.0D),
    NORTH_WEST(-1.0D, -1.0D);

    private final double xMulti;

    private final double zMulti;

    Direction(double paramDouble1, double paramDouble2) {
        this.xMulti = paramDouble1;
        this.zMulti = paramDouble2;
    }

    public Direction next() {
        return values()[(ordinal() + 1) % (values()).length];
    }

    public Location addTo(Location location, int amount) {
        return location.clone().add(amount * this.xMulti, 0.0D, amount * this.zMulti);
    }
}
