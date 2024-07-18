package ro.steve.gamesettings.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    
    private final Map<String, YamlConfiguration> configs;
    private final Plugin plugin;
    
    public Configuration(Plugin plugin) {
        configs = new HashMap<>();
        this.plugin = plugin;
        init("messages", "buy", "confirm");
    }

    private void init(String... cfg) {
        for (String config : cfg) {
            File file = new File(plugin.getDataFolder(), config + ".yml");
            long time = System.currentTimeMillis();
            if (!file.exists()) {
                plugin.saveResource(config + ".yml", true);
                plugin.getLogger().info(config + ".yml doesn't exists, creating a new one...");
            }
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            yml.options().copyDefaults(true);
            configs.put(config, yml);
            plugin.getLogger().info("Loading " + config + ".yml... This action took " + (System.currentTimeMillis() - time) + "ms");
        }
    }
    
    public YamlConfiguration getConfig(String config) {
        if (configs.get(config) == null) {
            File file = new File(plugin.getDataFolder(), config + ".yml");
            if (!file.exists()) {
                plugin.saveResource(config + ".yml", true);
            }
            return YamlConfiguration.loadConfiguration(file);
        }
        return configs.get(config);
    }
}
