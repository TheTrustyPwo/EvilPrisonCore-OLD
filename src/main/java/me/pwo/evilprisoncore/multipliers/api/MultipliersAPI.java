package me.pwo.evilprisoncore.multipliers.api;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.PlayerMultiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public interface MultipliersAPI {
    PlayerMultiplier getPlayerMultiplier(Player player);

    void addPlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type);

    void removePlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type);

    RankMultiplier getRankMultiplier(Player player);

    default double getTotalToDeposit(Player player, double deposit, MultiplierType type) {
        return deposit * (1.0D + getPlayerMultiplier(player).getMultiplierSet().getMulti(type).getMultiplier());
    }
}
