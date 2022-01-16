package me.pwo.evilprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.database.SQLDatabase;
import org.bukkit.configuration.file.FileConfiguration;

public class MySQLDatabase extends SQLDatabase {
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;

    public MySQLDatabase(EvilPrisonCore evilPrisonCore) {
        super(evilPrisonCore);
        FileConfiguration config = EvilPrisonCore.getInstance().getFileUtils().getConfig("config.yml").get();
        this.host = config.getString("mysql.host");
        this.database = config.getString("mysql.database");
        this.username = config.getString("mysql.username");
        this.password = config.getString("mysql.password");
        this.port = config.getInt("mysql.port");
        connect();
    }

    public void connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("EvilPrisonMySQLPool");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true");
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaxLifetime(60000L);
        hikariConfig.setIdleTimeout(45000L);
        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        this.hikari = new HikariDataSource(hikariConfig);
        createTables();
    }

    public void createTables() {
        Schedulers.async().run(() -> {
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_AutoMiner(UUID varchar(36) NOT NULL UNIQUE, Time int, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Blocks(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, Tier int, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Tokens(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Gems(UUID varchar(36) NOT NULL UNIQUE, Gems bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Credits(UUID varchar(36) NOT NULL UNIQUE, Credits bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Ranks(UUID varchar(36) NOT NULL UNIQUE, `Rank` bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Multipliers(ID int NOT NULL AUTO_INCREMENT, UUID varchar(36) NOT NULL, Multiplier double(12, 2), TimeLeft bigint, Type varchar(36), Source varchar(36), PRIMARY KEY (ID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Gangs(UUID varchar(36) NOT NULL UNIQUE, Name varchar(36) NOT NULL UNIQUE, Owner varchar(36) NOT NULL, Value bigint default 0, Members text, PRIMARY KEY (UUID, Name))");
        });
    }
}
