package ro.steve.gamesettings;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ro.steve.gamesettings.commands.GameSettingsCommands;
import ro.steve.gamesettings.configuration.Configuration;
import ro.steve.gamesettings.gamesettings.GameSettingsStorage;
import ro.steve.gamesettings.util.Storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GameSettingsMain extends JavaPlugin {

    private static Economy econ = null;

    private static GameSettingsMain INSTANCE;
    private Configuration C;
    private GameSettingsStorage GSS;

    public void onEnable() {
        INSTANCE = this;
        setupEconomy();
        C = new Configuration(this);
        GSS = new GameSettingsStorage();
        Storage S = new Storage();
        S.getGameSettingsList().forEach(g -> getServer().getPluginManager().registerEvents(g, this));
        new GameSettingsCommands(this, "gens", "sellall", "clearchat");
    }

    public void onDisable() {
        if (!GSS.getEventBlocks().isEmpty()) {
            File file = new File(this.getDataFolder(), "blocks.yml");
            YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
            Map<String, String> locations = new HashMap<>();
            GSS.getEventBlocks().forEach((l, t) -> {
                String loc = l.getWorld().getName() + ";" + (int) l.getX() + ";" + (int) l.getY() + ";" + (int) l.getZ();
                locations.put(loc, t);
            });
            AtomicInteger times = new AtomicInteger(1);
            locations.forEach((l, t) -> {
                y.set("Blocks." + times + ".location", l);
                y.set("Blocks." + times + ".tier", t);
                times.getAndIncrement();
            });
            try {
                y.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static GameSettingsMain getInstance() {
        return INSTANCE;
    }

    public Configuration getConfiguration() {
        return C;
    }

    public GameSettingsStorage getGameSettingsStorage() {
        return GSS;
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public static Economy getEconomy() {
        return econ;
    }
}
