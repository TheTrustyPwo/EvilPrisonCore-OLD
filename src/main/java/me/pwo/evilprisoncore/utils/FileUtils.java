package me.pwo.evilprisoncore.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileUtils {
    private final JavaPlugin plugin;

    private final HashMap<String, Config> configs = new HashMap<>();

    public FileUtils(JavaPlugin paramJavaPlugin) {
        this.plugin = paramJavaPlugin;
    }

    public Config getConfig(String paramString) {
        if (!this.configs.containsKey(paramString))
            this.configs.put(paramString, new Config(paramString));
        return this.configs.get(paramString);
    }

    public Config saveConfig(String paramString) {
        return getConfig(paramString).save();
    }

    public Config reloadConfig(String paramString) {
        return getConfig(paramString).reload();
    }

    public class Config {
        private final String name;

        private File file;

        private YamlConfiguration config;

        public Config(String param1String) {
            this.name = param1String;
        }

        public Config save() {
            if (this.config == null || this.file == null)
                return this;
            try {
                if (this.config.getConfigurationSection("").getKeys(true).size() != 0)
                    this.config.save(this.file);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            return this;
        }

        public YamlConfiguration get() {
            if (this.config == null)
                reload();
            return this.config;
        }

        public Config saveDefaultConfig() {
            this.file = new File(FileUtils.this.plugin.getDataFolder(), this.name);
            FileUtils.this.plugin.saveResource(this.name, false);
            return this;
        }

        public Config reload() {
            if (this.file == null)
                this.file = new File(FileUtils.this.plugin.getDataFolder(), this.name);
            this.config = YamlConfiguration.loadConfiguration(this.file);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(FileUtils.this.plugin.getResource(this.name), StandardCharsets.UTF_8);
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(inputStreamReader);
                this.config.setDefaults(yamlConfiguration);
            } catch (NullPointerException ignored) {}
            return this;
        }

        public Config copyDefaults(boolean param1Boolean) {
            get().options().copyDefaults(param1Boolean);
            return this;
        }

        public Config set(String param1String, Object param1Object) {
            get().set(param1String, param1Object);
            return this;
        }

        public Object get(String param1String) {
            return get().get(param1String);
        }
    }
}
