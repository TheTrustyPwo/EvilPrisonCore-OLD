package me.pwo.evilprisoncore.casino.crash;


import me.pwo.evilprisoncore.EvilPrisonCore;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class Crash {
    // btw u have to save previous round crash numbers
    // in sql or something
    // like jailsmc
    // they show them at the side
    private EvilPrisonCore plugin;
    private OfflinePlayer gamePlayer;
    private int amount;
    private float multiplier;
    private HashMap<OfflinePlayer,Integer> PlayerAmount;
    private HashMap<OfflinePlayer,Float> PlayerMultiplier;
    // there can be multiple players in 1 session
    
    public int getBetAmount(OfflinePlayer gamePlayer) {
            return PlayerAmount.get(gamePlayer);
    }

    public float getBetMultiplier(OfflinePlayer player) {
            return PlayerMultiplier.get(player);
    }

    public void setBetMultiplier(float gamemulti) {
            multiplier = gamemulti;
            PlayerMultiplier.put(gamePlayer,gamemulti);
    }
    
    public void setBetAmount(int gameamount) {
        amount = gameamount;
        PlayerAmount.put(gamePlayer,gameamount);
    }
    
    public OfflinePlayer getGamePlayer() {
        return gamePlayer; // this dum why have u only made like 30 lines at least like 50 maybe
    } //wait how do u lose or can u even lose
    
    public void gameReset(OfflinePlayer player) {
        PlayerAmount.remove(player);
        PlayerMultiplier.remove(player);
        //save in logs or something idk
    }

    public void saveLogs(OfflinePlayer player, int gameamount, float gamemulti) throws IOException {
        File crashLogsDir = new File(plugin.getDataFolder(), "/crashLogs");
        try {
            crashLogsDir.mkdir();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        File crashLogs = new File(plugin.getDataFolder(), "/crashLogs/log.yml");
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(crashLogs)));
        LoggerContext lc = new LoggerContext("L");
        ConfigurationSource ls = new ConfigurationSource((InputStream) input);
        YamlConfiguration yc = new YamlConfiguration(lc,ls);
        //ok i gtg er this not complete but i dont think theres errors so yea





    }

}
