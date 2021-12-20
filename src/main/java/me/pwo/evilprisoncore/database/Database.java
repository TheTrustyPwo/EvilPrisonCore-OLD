package me.pwo.evilprisoncore.database;

import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.gangs.gang.Gang;
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

    public abstract void updatePlayerTokens(OfflinePlayer player, long amount);

    public abstract void updatePlayerRank(OfflinePlayer player, long amount);

    public abstract void updatePlayerGems(OfflinePlayer player, long amount);

    public abstract Map<UUID, Long> getTop10Tokens();

    public abstract Map<UUID, Long> getTop10Ranks();

    public abstract Map<UUID, Long> getTop10Gems();

    public abstract void addIntoTokens(OfflinePlayer player);

    public abstract void addIntoRanks(OfflinePlayer player);

    public abstract void addIntoGems(OfflinePlayer player);

    public abstract int getPlayerAutoMinerTime(OfflinePlayer player);

    public abstract void saveAutoMiner(Player player, int amount);

    public abstract void removeExpiredAutoMiners();

    public abstract ResultSet getAllMineData();

    public abstract ResultSet getPlayerMineData(OfflinePlayer player);

    public abstract void addIntoMineData(OfflinePlayer player);

    public abstract void givePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player);

    public abstract void revokePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player);
    
    public abstract void banPlayerPrivateMine(OfflinePlayer owner, OfflinePlayer player);

    public abstract void unbanPlayerPrivateMine(OfflinePlayer owner, OfflinePlayer player);
    
    public abstract List<UUID> getBannedPlayersPrivateMine(OfflinePlayer owner);
    
    public abstract void getAccessPlayersPrivateMine(OfflinePlayer owner);

    public abstract List<Gang> getAllGangs();

    public abstract void updateGang(Gang gang);

    public abstract void deleteGang(Gang gang);

    public abstract void createGang(Gang gang);
}
