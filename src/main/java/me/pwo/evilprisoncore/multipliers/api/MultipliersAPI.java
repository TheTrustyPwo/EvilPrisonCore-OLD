package me.pwo.evilprisoncore.multipliers.api;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface MultipliersAPI {
    List<Multiplier> getPlayerMultiplier(Player player);

    void addPlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type);

    void removePlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type);

    RankMultiplier getRankMultiplier(Player player);

    double getTotalToDeposit(Player player, double deposit, MultiplierType type);
}
