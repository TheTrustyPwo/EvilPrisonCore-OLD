package me.pwo.evilprisoncore.ranks.api;

import me.pwo.evilprisoncore.ranks.model.RankModel;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface RanksAPI {
    RankModel getPlayerRank(OfflinePlayer paramPlayer);

    RankModel getNextPlayerRank(Player paramPlayer);

    int getRankupProgress(Player paramPlayer);

    void setPlayerRank(Player paramPlayer, RankModel paramRank);
}
