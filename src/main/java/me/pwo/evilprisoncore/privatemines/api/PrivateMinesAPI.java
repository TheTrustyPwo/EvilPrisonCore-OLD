package me.pwo.evilprisoncore.privatemines.api;

import me.pwo.evilprisoncore.privatemines.mine.Mine;
import org.bukkit.Location;

public interface PrivateMinesAPI {
    Mine getMineByLocation(Location location);
}
