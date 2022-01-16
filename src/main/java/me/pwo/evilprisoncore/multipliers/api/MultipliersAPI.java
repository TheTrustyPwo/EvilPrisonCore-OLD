package me.pwo.evilprisoncore.multipliers.api;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface MultipliersAPI {
    List<Multiplier> getPlayerMultiplier(Player player);

    void addPlayerMultiplier(Player player, int id, double multi, TimeUnit timeUnit, int time);

    void removePlayerMultiplier(Player player, int id, double multi, TimeUnit timeUnit, int time);

    void givePlayerMultiplier(Player player, double amount, TimeUnit timeUnit, int time, MultiplierType multiplierType, MultiplierSource multiplierSource);

    void deletePlayerMultiplier(Player player, int id);

    RankMultiplier getRankMultiplier(Player player);

    double getTotalToDeposit(Player player, double deposit, MultiplierType type);
}
