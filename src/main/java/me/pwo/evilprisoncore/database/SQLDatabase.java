package me.pwo.evilprisoncore.database;

import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
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
    protected static final long MAX_LIFETIME = TimeUnit.DAYS.toMillis(1L);
    protected static final long CONNECTION_TIMEOUT = TimeUnit.MINUTES.toMillis(60L);
    protected static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10L);

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
        try (Connection connection = this.hikari.getConnection(); ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Ranks ORDER BY Rank DESC LIMIT 10").executeQuery()) {
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
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Credits ORDER BY Credits DESC LIMIT 10").executeQuery();
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
        try (Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM EvilPrison_AutoMiner WHERE Time <= 0")) {
            preparedStatement.execute();
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public int getPlayerAutoMinerTime(OfflinePlayer paramOfflinePlayer) {
        try (Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_AutoMiner WHERE UUID=?")) {
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
        try (Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO EvilPrison_AutoMiner VALUES (?,?) ON DUPLICATE KEY UPDATE Time=?")) {
            preparedStatement.setString(1, paramPlayer.getUniqueId().toString());
            preparedStatement.setInt(2, paramInt);
            preparedStatement.setInt(3, paramInt);
            preparedStatement.execute();
        } catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    @Override
    public void removeExpiredMultipliers() {
        try (Connection connection = this.hikari.getConnection()) {
            connection.prepareStatement(
                    "DELETE FROM EvilPrison_Multipliers WHERE TimeLeft<" + Time.nowMillis()).execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Override
    public List<Multiplier> getPlayerMultipliers(Player player) {
        List<Multiplier> list = new ArrayList<>();
        try (Connection connection = this.hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EvilPrison_Multipliers WHERE UUID=?");
            preparedStatement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(new Multiplier(
                            resultSet.getInt("ID"),
                            resultSet.getDouble("Multiplier"),
                            resultSet.getLong("TimeLeft"),
                            MultiplierType.valueOf(resultSet.getString("Type")),
                            MultiplierSource.valueOf(resultSet.getString("Source"))));
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        System.out.println(list);
        return list;
    }

    @Override
    public void savePlayerMultipliers(Player player, List<Multiplier> multipliers) {
        try (Connection connection = this.hikari.getConnection()) {
            System.out.println(multipliers);
            for (Multiplier multiplier : multipliers) {
                PreparedStatement preparedStatement;
                if (multiplier.getId() == null) {
                    preparedStatement = connection.prepareStatement(
                            "INSERT INTO EvilPrison_Multipliers (UUID, Multiplier, TimeLeft, Type, Source) VALUES (?,?,?,?,?)");
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setDouble(2, multiplier.getMultiplier());
                    preparedStatement.setLong(3, multiplier.getEndTime());
                    preparedStatement.setString(4, multiplier.getMultiplierType().name());
                    preparedStatement.setString(5, multiplier.getMultiplierSource().name());
                } else {
                    preparedStatement = connection.prepareStatement(
                            "UPDATE EvilPrison_Multipliers SET TimeLeft=? WHERE ID=?");
                    preparedStatement.setLong(1, multiplier.getEndTime());
                    preparedStatement.setInt(2, multiplier.getId());
                }
                preparedStatement.execute();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public abstract void connect();

    public abstract void runSQLUpdates();

    public abstract void createTables();
}
