package me.pwo.evilprisoncore.multipliers.api;

import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MultipliersAPIImpl implements MultipliersAPI {
    private final Multipliers multipliers;

    public MultipliersAPIImpl(Multipliers multipliers) {
        this.multipliers = multipliers;
    }

    @Override
    public List<Multiplier> getPlayerMultiplier(Player player) {
        return this.multipliers.getMultipliersManager().getPlayerMultipliers(player);
    }

    @Override
    public void addPlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type) {
        // this.multipliers.getMultipliersManager().addPlayerMultiplier(player, multi, timeUnit, time, type);
    }

    @Override
    public void removePlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type) {
        // this.multipliers.getMultipliersManager().addPlayerMultiplier(player, -1 * multi, timeUnit, -1 * time, type);
    }

    @Override
    public RankMultiplier getRankMultiplier(Player player) {
        return this.multipliers.getMultipliersManager().getRankMultiplier(player);
    }

    @Override
    public double getTotalToDeposit(Player player, double deposit, MultiplierType type) {
        return this.multipliers.getMultipliersManager().getTotalPlayerMultiplier(player, type) * deposit;
    }
}
