package ro.steve.gamesettings.gamesettings;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import ro.steve.gamesettings.GameSettingsMain;

import java.io.File;

public interface GameSettings extends Listener {

    default YamlConfiguration loadConfig(String name) {
        long time = System.currentTimeMillis();
        GameSettingsMain rem = GameSettingsMain.getInstance();
        File file = new File(rem.getDataFolder(), name + ".yml");
        if (!file.exists()) {
            rem.saveResource(name + ".yml", true);
            rem.getLogger().info(name + ".yml doesn't exists... Creating a new configuration...");
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file).options().copyDefaults(true).configuration();
        rem.getLogger().info("Loading " + name + ".yml, please wait... This action took " + (System.currentTimeMillis() - time) + "ms");
        return yml;
    }
}
