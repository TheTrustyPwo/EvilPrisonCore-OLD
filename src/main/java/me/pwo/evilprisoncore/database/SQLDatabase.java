package me.pwo.evilprisoncore.database;

import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.gangs.gang.Gang;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;
import me.pwo.evilprisoncore.multipliers.model.Multiplier;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class SQLDatabase extends Database {
    protected static final AtomicInteger POOL_COUNTER;
    protected static final int MAXIMUM_POOL_SIZE;
    protected static final int MINIMUM_IDLE;
    protected static final long MAX_LIFETIME;
    protected static final long CONNECTION_TIMEOUT;
    protected static final long LEAK_DETECTION_THRESHOLD;

    static {
        POOL_COUNTER = new AtomicInteger(0);
        MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;
        MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);
        MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30L);
        CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(60L);
        LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(5L);
    }

    protected EvilPrisonCore plugin;
    protected HikariDataSource hikari;

    public SQLDatabase(EvilPrisonCore evilPrisonCore) {
        super(evilPrisonCore);
        this.plugin = evilPrisonCore;
    }

    public synchronized void execute(String statement, Object... args) {
        try (Connection connection = this.hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            if (args != null)
                for (byte b = 0; b < args.length; b++)
                    preparedStatement.setObject(b + 1, args[b]);
            this.plugin.debug("MySQL::ExecutingStatement >> %s ");
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            if (sqlException.getErrorCode() != 1061)
                sqlException.printStackTrace();
        }
    }

    public void executeAsync(String statement, Object... args) {
        Schedulers.async().run(() -> execute(statement, args));
    }

    public void close() {
        if (this.hikari != null) {
            this.hikari.close();
            this.plugin.getLogger().info("Closing SQL Connection");
        }
    }

    @Override
    public int getNextAutoIncrementValue(String tableName) {
        try (Connection connection = this.hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=?")) {
            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("AUTO_INCREMENT");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return 0;
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
        execute("UPDATE EvilPrison_Ranks SET `Rank`=? WHERE UUID=?", amount, paramOfflinePlayer.getUniqueId().toString());
    }

    public Map getTop10Ranks() {
        LinkedHashMap<UUID, Long> linkedHashMap = new LinkedHashMap<>();
        try (Connection connection = this.hikari.getConnection(); ResultSet resultSet = connection.prepareStatement("SELECT * FROM EvilPrison_Ranks ORDER BY Rank DESC LIMIT 10").executeQuery()) {
            while (resultSet.next())
                linkedHashMap.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getLong("Rank"));
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
        return list;
    }

    @Override
    public void savePlayerMultipliers(Player player, List<Multiplier> multipliers) {
        try (Connection connection = this.hikari.getConnection()) {
            for (Multiplier multiplier : multipliers) {
                if (multiplier.getId() == -1) continue;
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO EvilPrison_Multipliers (ID, UUID, Multiplier, TimeLeft, Type, Source) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE Multiplier=?, TimeLeft=?");
                preparedStatement.setInt(1, multiplier.getId());
                preparedStatement.setString(2, player.getUniqueId().toString());
                preparedStatement.setDouble(3, multiplier.getMultiplier());
                preparedStatement.setLong(4, multiplier.getEndTime());
                preparedStatement.setString(5, multiplier.getMultiplierType().name());
                preparedStatement.setString(6, multiplier.getMultiplierSource().name());
                preparedStatement.setDouble(7, multiplier.getMultiplier());
                preparedStatement.setLong(8, multiplier.getEndTime());
                preparedStatement.execute();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Override
    public List<Gang> getAllGangs() {
        List<Gang> gangs = new ArrayList<>();
        try (Connection connection = this.hikari.getConnection(); PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM EvilPrison_Gangs", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                UUID gangUUID;
                try {
                    gangUUID = UUID.fromString(resultSet.getString("UUID"));
                } catch (Exception exception) {
                    gangUUID = UUID.randomUUID();
                    resultSet.updateString("UUID", gangUUID.toString());
                    resultSet.updateRow();
                }
                String gangName = resultSet.getString("Name");
                UUID gangOwner = UUID.fromString(resultSet.getString("Owner"));
                List<UUID> members = new ArrayList<>();
                Arrays.stream(resultSet.getString("Members").split(",")).forEach(member -> members.add(UUID.fromString(member)));
                long gangTrophies = resultSet.getLong("Trophies");
                gangs.add(new Gang(gangUUID, gangName, gangOwner, members, gangTrophies));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return gangs;
    }

    @Override
    public void updateGang(Gang gang) {
        execute("UPDATE EvilPrison_Gangs SET Members=?, Name=?, Trophies=? WHERE UUID=?",
                StringUtils.join(gang.getMembersOffline().stream().map(OfflinePlayer::getUniqueId).map(UUID::toString).toArray(), ","),
                gang.getGangName(),
                gang.getGangTrophies(),
                gang.getGangId().toString());
    }

    @Override
    public void createGang(Gang gang) {
        executeAsync("INSERT IGNORE INTO EvilPrison_Gangs(UUID, Name, Owner, Members) VALUES(?,?,?,?)",
                gang.getGangId().toString(),
                gang.getGangName(),
                gang.getGangOwner().toString(),
                "");
    }

    @Override
    public void deleteGang(Gang gang) {
        executeAsync("DELETE FROM EvilPrison_Gangs WHERE UUID=?", gang.getGangId().toString());
    }

    public abstract void connect();

    public abstract void createTables();
}
