package me.pwo.evilprisoncore.multipliers.api;

import me.pwo.evilprisoncore.multipliers.Multipliers;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.PlayerMultiplier;
import me.pwo.evilprisoncore.multipliers.model.RankMultiplier;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class MultipliersAPIImpl implements MultipliersAPI {
    private final Multipliers multipliers;

    public MultipliersAPIImpl(Multipliers multipliers) {
        this.multipliers = multipliers;
    }

    @Override
    public PlayerMultiplier getPlayerMultiplier(Player player) {
        return this.multipliers.getMultipliersManager().getPlayerMultiplier(player);
    }

    @Override
    public void addPlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type) {
        this.multipliers.getMultipliersManager().addPlayerMultiplier(player, multi, timeUnit, time, type);
    }

    @Override
    public void removePlayerMultiplier(Player player, double multi, TimeUnit timeUnit, int time, MultiplierType type) {
        this.multipliers.getMultipliersManager().addPlayerMultiplier(player, -1 * multi, timeUnit, -1 * time, type);
    }

    @Override
    public RankMultiplier getRankMultiplier(Player player) {
        return this.multipliers.getMultipliersManager().getRankMultiplier(player);
    }
}
