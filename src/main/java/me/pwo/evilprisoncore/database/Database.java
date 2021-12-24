package me.pwo.evilprisoncore.database;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Database {
    protected final EvilPrisonCore plugin;

    public Database(EvilPrisonCore evilPrisonCore) { this.plugin = evilPrisonCore; }

    public abstract long getPlayerTokens(OfflinePlayer player);

    public abstract long getPlayerRank(OfflinePlayer player);

    public abstract long getPlayerGems(OfflinePlayer player);

    public abstract long getPlayerCredits(OfflinePlayer player);

    public abstract long getPlayerBlocks(OfflinePlayer player);

    public abstract void updatePlayerTokens(OfflinePlayer player, long amount);

    public abstract void updatePlayerRank(OfflinePlayer player, long amount);

    public abstract void updatePlayerGems(OfflinePlayer player, long amount);

    public abstract void updatePlayerCredits(OfflinePlayer player, long amount);

    public abstract void updatePlayerBlocks(OfflinePlayer player, long amount);

    public abstract Map<UUID, Long> getTop10Tokens();

    public abstract Map<UUID, Long> getTop10Ranks();

    public abstract Map<UUID, Long> getTop10Gems();

    public abstract Map<UUID, Long> getTop10Credits();

    public abstract Map<UUID, Long> getTop10Blocks();

    public abstract void addIntoTokens(OfflinePlayer player);

    public abstract void addIntoRanks(OfflinePlayer player);

    public abstract void addIntoGems(OfflinePlayer player);

    public abstract void addIntoCredits(OfflinePlayer player);

    public abstract void addIntoBlocks(OfflinePlayer player);

    public abstract int getPlayerBlockTier(OfflinePlayer player);

    public abstract void updatePlayerBlockTier(OfflinePlayer player, int tier);

    public abstract int getPlayerAutoMinerTime(OfflinePlayer player);

    public abstract void saveAutoMiner(Player player, int amount);

    public abstract void removeExpiredAutoMiners();

    public abstract List<Mine> getAllMineData();

    public abstract Mine getPlayerMineData(OfflinePlayer player);

    public abstract void addIntoMineData(OfflinePlayer player);

    public abstract void givePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player);

    public abstract void revokePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player);
    
    public abstract List<UUID> getAccessPlayersPrivateMine(OfflinePlayer owner);
}
