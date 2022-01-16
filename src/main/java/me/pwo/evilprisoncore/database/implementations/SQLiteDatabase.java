package me.pwo.evilprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.Schedulers;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.database.SQLDatabase;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;

public class SQLiteDatabase extends SQLDatabase {
    private final String filePath;

    public SQLiteDatabase(EvilPrisonCore evilPrisonCore) {
        super(evilPrisonCore);
        this.plugin.getLogger().info("Using SQLite (local) database.");
        this.filePath = this.plugin.getDataFolder().getPath() + File.separator + "playerdata.sqlite";
        this.plugin.getLogger().info(String.format("Path to SQLite Database %s is %s", "playerdata.sqlite", this.filePath));
        createDBFile();
        connect();
    }

    public void connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("evilprison-1");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + this.filePath);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setMinimumIdle(MINIMUM_IDLE);
        hikariConfig.setMaxLifetime(MAX_LIFETIME);
        hikariConfig.setConnectionTimeout(0L);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setLeakDetectionThreshold(0L);
        this.hikari = new HikariDataSource(hikariConfig);
        createTables();
    }

    public void createTables() {
        Schedulers.async().run(() -> {
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Tokens(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS EvilPrison_Ranks(UUID varchar(36) NOT NULL UNIQUE, Rank bigint, primary key (UUID))");
        });
    }

    private void createDBFile() {
        File file = new File(this.filePath);
        try {
            file.createNewFile();
        } catch (IOException ioException) {
            this.plugin.getLogger().warning(String.format("Unable to create %s", "playerdata.sqlite"));
            ioException.printStackTrace();
        }
    }

    public void addIntoTokens(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT OR IGNORE INTO EvilPrison_Tokens VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }

    public void addIntoRanks(OfflinePlayer paramOfflinePlayer) {
        execute("INSERT OR IGNORE INTO EvilPrison_Ranks VALUES(?,?)", paramOfflinePlayer.getUniqueId().toString(), 0);
    }
}
