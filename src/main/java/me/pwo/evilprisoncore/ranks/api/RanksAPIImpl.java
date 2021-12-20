package me.pwo.evilprisoncore.ranks.api;

import me.pwo.evilprisoncore.ranks.Ranks;
import me.pwo.evilprisoncore.ranks.model.RankModel;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class RanksAPIImpl implements RanksAPI {
    private final Ranks ranks;

    public RanksAPIImpl(Ranks ranks) {
        this.ranks = ranks;
    }

    public RankModel getPlayerRank(OfflinePlayer paramPlayer) {
        return this.ranks.getRankManager().getPlayerRank(paramPlayer);
    }

    public RankModel getNextPlayerRank(Player paramPlayer) {
        return this.ranks.getRankManager().getNextRank(getPlayerRank(paramPlayer));
    }

    public int getRankupProgress(Player paramPlayer) {
        return this.ranks.getRankManager().getRankUpProgress(paramPlayer);
    }

    public void setPlayerRank(Player paramPlayer, RankModel paramRank) {
        this.ranks.getRankManager().setRank(paramPlayer, paramRank, null);
    }
}
