package me.pwo.evilprisoncore.database;

import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public abstract class SQLDatabase extends Database {
    protected EvilPrisonCore plugin;
    protected HikariDataSource hikari;
    protected static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);
    protected static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;
    protected static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);
    protected static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30L);
    protected static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(60L);
    protected static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(5L);

    public SQLDatabase(EvilPrisonCore evilPrisonCore) {
        super(evilPrisonCore);
        this.plugin = evilPrisonCore;
    }

    public synchronized void execute(String paramString, Object... paramVarArgs) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(paramString);
            if (paramVarArgs != null)
                for (byte b = 0; b < paramVarArgs.length; b++)
                    preparedStatement.setObject(b + 1, paramVarArgs[b]);
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void executeAsync(String paramString, Object... paramVarArgs) {
        Schedulers.async().run(() -> execute(paramString, paramVarArgs));
    }

    public void close() {
        if (this.hikari != null) {
            this.hikari.close();
            this.plugin.getLogger().info("Closing SQL Connection");
        }
    }

    @Override
    public long getPlayerTokens(OfflinePlayer paramOfflinePlayer) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_Tokens WHERE UUID=?");
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getLong("Tokens");
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0L;
    }

    @Override
    public void updatePlayerTokens(OfflinePlayer paramOfflinePlayer, long paramLong) {
        executeAsync("UPDATE EvilPrison_Tokens SET Tokens=? WHERE UUID=?", paramLong, paramOfflinePlayer.getUniqueId().toString());
    }

    @Override
    public Map<UUID, Long> getTop10Tokens() {
        LinkedHashMap<UUID, Long> linkedHashMap = new LinkedHashMap<>();
        try {
            Connection connection = this.hikari.getConnection();
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Tokens ORDER BY Tokens DESC LIMIT 10").executeQuery();
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getLong("Tokens"));
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return linkedHashMap;
    }

    @Override
    public void addIntoTokens(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT IGNORE INTO EvilPrison_Tokens VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }

    public long getPlayerRank(OfflinePlayer paramOfflinePlayer) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_Ranks WHERE UUID=?");
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt("Rank");
            }
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0;
    }

    public void updatePlayerRank(OfflinePlayer paramOfflinePlayer, long amount) {
        execute("UPDATE EvilPrison_Ranks SET Rank=? WHERE UUID=?", amount, paramOfflinePlayer.getUniqueId().toString());
    }

    public Map getTop10Ranks() {
        LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
        try(Connection connection = this.hikari.getConnection(); ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Ranks ORDER BY Rank DESC LIMIT 10").executeQuery()) {
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getInt("Rank"));
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return linkedHashMap;
    }

    public void addIntoRanks(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT IGNORE INTO EvilPrison_Ranks VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }

    @Override
    public long getPlayerCredits(OfflinePlayer paramOfflinePlayer) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_Credits WHERE UUID=?");
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getLong("Credits");
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0L;
    }

    @Override
    public void updatePlayerCredits(OfflinePlayer paramOfflinePlayer, long paramLong) {
        executeAsync("UPDATE EvilPrison_Credits SET Credits=? WHERE UUID=?", paramLong, paramOfflinePlayer.getUniqueId().toString());
    }

    @Override
    public Map<UUID, Long> getTop10Credits() {
        LinkedHashMap<UUID, Long> linkedHashMap = new LinkedHashMap<>();
        try {
            Connection connection = this.hikari.getConnection();
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Credits ORDER BY Gems DESC LIMIT 10").executeQuery();
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getLong("Credits"));
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return linkedHashMap;
    }

    @Override
    public void addIntoCredits(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT IGNORE INTO EvilPrison_Credits VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }

    @Override
    public long getPlayerGems(OfflinePlayer paramOfflinePlayer) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_Gems WHERE UUID=?");
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getLong("Gems");
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0L;
    }

    @Override
    public void updatePlayerGems(OfflinePlayer paramOfflinePlayer, long paramLong) {
        executeAsync("UPDATE EvilPrison_Gems SET Gems=? WHERE UUID=?", paramLong, paramOfflinePlayer.getUniqueId().toString());
    }

    @Override
    public Map<UUID, Long> getTop10Gems() {
        LinkedHashMap<UUID, Long> linkedHashMap = new LinkedHashMap<>();
        try {
            Connection connection = this.hikari.getConnection();
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Gems ORDER BY Gems DESC LIMIT 10").executeQuery();
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getLong("Gems"));
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return linkedHashMap;
    }

    @Override
    public void addIntoGems(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT IGNORE INTO EvilPrison_Gems VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }

    @Override
    public long getPlayerBlocks(OfflinePlayer paramOfflinePlayer) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID, Blocks FROM EvilPrison_Blocks WHERE UUID=?");
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getLong("Blocks");
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0L;
    }

    @Override
    public void updatePlayerBlocks(OfflinePlayer paramOfflinePlayer, long paramLong) {
        executeAsync("UPDATE EvilPrison_Blocks SET Blocks=? WHERE UUID=?", paramLong, paramOfflinePlayer.getUniqueId().toString());
    }

    @Override
    public Map<UUID, Long> getTop10Blocks() {
        LinkedHashMap<UUID, Long> linkedHashMap = new LinkedHashMap<>();
        try {
            Connection connection = this.hikari.getConnection();
            ResultSet resultSet = connection.prepareStatement("SELECT UUID, Blocks FROM EvilPrison_Blocks ORDER BY Blocks DESC LIMIT 10").executeQuery();
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getLong("Blocks"));
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return linkedHashMap;
    }

    @Override
    public void addIntoBlocks(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT IGNORE INTO EvilPrison_Blocks VALUES(?,?,?)", paramOfflinePlayer.getUniqueId().toString(), 0, 0);
    }

    @Override
    public int getPlayerBlockTier(OfflinePlayer player) {
        try {
            Connection connection = this.hikari.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Tier FROM EvilPrison_Blocks WHERE UUID=?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt("Tier");
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0;
    }

    @Override
    public void updatePlayerBlockTier(OfflinePlayer player, int tier) {
        execute("UPDATE EvilPrison_Blocks SET Tier=? WHERE UUID=?", tier, player.getUniqueId().toString());
    }

    public void removeExpiredAutoMiners() {
        try(Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM EvilPrison_AutoMiner WHERE Time <= 0")) {
            preparedStatement.execute();
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public int getPlayerAutoMinerTime(OfflinePlayer paramOfflinePlayer) {
        try(Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_AutoMiner WHERE UUID=?")) {
            preparedStatement.setString(1, paramOfflinePlayer.getUniqueId().toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt("time");
            }
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return 0;
    }

    public void saveAutoMiner(Player paramPlayer, int paramInt) {
        try(Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO EvilPrison_AutoMiner VALUES (?,?) ON DUPLICATE KEY UPDATE Time=?")) {
            preparedStatement.setString(1, paramPlayer.getUniqueId().toString());
            preparedStatement.setInt(2, paramInt);
            preparedStatement.setInt(3, paramInt);
            preparedStatement.execute();
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    @Override
    public List<Mine> getAllMineData() {
        List<Mine> list = new ArrayList<>();
        try(PreparedStatement preparedStatement = this.hikari.getConnection().prepareStatement(
                "SELECT * FROM EvilPrison_PMines")) {
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UUID owner = UUID.fromString(resultSet.getString("UUID"));
                    list.add(new Mine(
                            owner,
                            resultSet.getBoolean("Public"),
                            resultSet.getDouble("Tax"),
                            Material.getMaterial(resultSet.getString("Material")),
                            getAccessPlayersPrivateMine(Players.getOfflineNullable(owner))
                    ));
                }
                return list;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    @Override
    public Mine getPlayerMineData(OfflinePlayer player) {
        try(PreparedStatement preparedStatement = this.hikari.getConnection().prepareStatement(
                "SELECT * FROM EvilPrison_PMines WHERE UUID=?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Mine(
                            UUID.fromString(resultSet.getString("UUID")),
                            resultSet.getBoolean("Public"),
                            resultSet.getDouble("Tax"),
                            Material.getMaterial(resultSet.getString("Material")),
                            getAccessPlayersPrivateMine(player)
                    );
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    @Override
    public void addIntoMineData(OfflinePlayer player) {
        execute("INSERT IGNORE INTO EvilPrison_PMines(UUID, Public, Tax, Material) VALUES(?,?,?,?)", player.getUniqueId().toString(), 0, 10.0D, Material.COBBLESTONE.toString());
    }

    @Override
    public void givePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player) {
        execute("INSERT INTO EvilPrison_PMines_Access(Player, PMines) VALUES (?,?)", owner.getUniqueId(), player.getUniqueId());
    }

    @Override
    public void revokePlayerPrivateMineAccess(OfflinePlayer owner, OfflinePlayer player) {
        execute("DELETE FROM EvilPrison_PMines_Access WHERE Player=?, PMine=?", owner.getUniqueId(), player.getUniqueId());
    }

    @Override
    public List<UUID> getAccessPlayersPrivateMine(OfflinePlayer owner) {
        List<UUID> list = new ArrayList<>();
        try(PreparedStatement preparedStatement = this.hikari.getConnection().prepareStatement(
                "SELECT * FROM EvilPrison_PMines_Access WHERE PMine=?")) {
            preparedStatement.setString(1, owner.getUniqueId().toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(UUID.fromString(resultSet.getString("Player")));
                }
                return list;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    public abstract void connect();

    public abstract void runSQLUpdates();

    public abstract void createTables();
}
