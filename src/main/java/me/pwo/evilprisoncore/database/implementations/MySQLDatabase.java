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
        hikariConfig.setPoolName("evilprison-" + POOL_COUNTER.getAndIncrement());
        hikariConfig.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setMinimumIdle(MINIMUM_IDLE);
        hikariConfig.setMaxLifetime(MAX_LIFETIME);
        hikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikariConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikariConfig.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        this.hikari = new HikariDataSource(hikariConfig);
        createTables();
    }

    public void runSQLUpdates() {}

    public void createTables() {
        Schedulers.async().run(() -> {
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Tokens(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Gems(UUID varchar(36) NOT NULL UNIQUE, Gems bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Credits(UUID varchar(36) NOT NULL UNIQUE, Credits bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Ranks(UUID varchar(36) NOT NULL UNIQUE, Rank bigint, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Gangs(ID varchar(36) NOT NULL UNIQUE, Name TEXT NOT NULL UNIQUE, Owner varchar(36) NOT NULL, Trophies bigint, PRIMARY KEY (ID)");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Gangs_Members(ID int NOT NULL AUTO_INCREMENT, GangID int NOT NULL, Player varchar(36) NOT NULL, PRIMARY KEY (ID), FOREIGN KEY (GangID) REFERENCES EvilPrison_Gangs(ID)");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_PMines(UUID varchar(36) NOT NULL UNIQUE, Public BOOL DEFAULT 1, Tax DOUBLE(4, 1) DEFAULT 10.0, Material TEXT, PRIMARY KEY (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_PMines_Access(ID int NOT NULL AUTO_INCREMENT, Player varchar(36) NOT NULL, PMine varchar(36) NOT NULL, PRIMARY KEY (ID), FOREIGN KEY (PMine) REFERENCES EvilPrison_PMines(UUID)");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_PMines_Banned(ID int NOT NULL AUTO_INCREMENT, Player varchar(36) NOT NULL, PMine varchar(36) NOT NULL, PRIMARY KEY (ID), FOREIGN KEY (PMine) REFERENCES EvilPrison_PMines(UUID)");
        });
    }
}
