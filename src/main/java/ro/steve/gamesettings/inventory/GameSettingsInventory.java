package ro.steve.gamesettings.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.item.ItemBuilder;
import ro.steve.gamesettings.util.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GameSettingsInventory implements InventoryHolder {

    private Location location;
    private final YamlConfiguration yml;
    private Inventory inv;
    private final String path;

    public GameSettingsInventory(String path, @Nullable Location location) {
        if (location != null) {
            this.location = location;
        }
        yml = GameSettingsMain.getInstance().getConfiguration().getConfig(path);
        this.path = path;
        createInventory();
        load();
    }

    private void createInventory() {
        inv = Bukkit.createInventory(this, yml.getInt("size"), Color.process(yml.getString("name")));
    }

    private void load() {
        YamlConfiguration gen = GameSettingsMain.getInstance().getConfiguration().getConfig("gens");
        if (path.equalsIgnoreCase("buy")) {
            yml.getConfigurationSection("Gens").getKeys(false).forEach(g -> {
                List<String> lore = new ArrayList<>();
                if (yml.get("Gens." + g + ".lore") != null) {
                    yml.getStringList("Gens." + g + ".lore").forEach(m -> {
                        if (m.contains("%upgrade_cost%")) {
                            m = m.replace("%upgrade_cost%", "" + gen.getInt("Gens." + yml.getString("Gens." + g + ".tier") + ".upgrade_cost"));
                        }
                        lore.add(Color.process(m));
                    });
                }
                ItemStack i = new ItemBuilder()
                        .amount(1)
                        .name(Color.process(yml.getString("Gens." + g + ".name")))
                        .lore(lore)
                        .type(Material.valueOf(gen.getString("Gens." + yml.getString("Gens." + g + ".tier") + ".material")))
                        .glowing(true)
                        .withPersistent("buy", PersistentDataType.STRING, yml.getString("Gens." + g + ".tier"))
                        .build();
                inv.setItem(yml.getInt("Gens." + g + ".slot"), i);
            });
        } else {
            yml.getConfigurationSection("Buttons").getKeys(false).forEach(g -> {
                List<String> lore = new ArrayList<>();
                String tier = GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().get(location);
                String next = "Tier_" + (Integer.parseInt(tier.substring(5)) + 1);
                if (yml.get("Buttons." + g + ".lore") != null) {
                    yml.getStringList("Buttons." + g + ".lore").forEach(m -> {
                        if (m.contains("%upgrade_cost%")) {
                            m = m.replace("%upgrade_cost%", "" + gen.getInt("Gens." + next + ".upgrade_cost"));
                        }
                        lore.add(Color.process(m));
                    });
                }
                ItemStack i = new ItemBuilder()
                        .amount(1)
                        .name(Color.process(yml.getString("Buttons." + g + ".name")))
                        .lore(lore)
                        .type(Material.valueOf(yml.getString("Buttons." + g + ".material")))
                        .withPersistent("action", PersistentDataType.STRING, g)
                        .withPersistent("location", PersistentDataType.STRING, location.getWorld().getName() + ";" + (int) location.getX() + ";" + (int) location.getY() + ";" + (int) location.getZ())
                        .build();
                inv.setItem(yml.getInt("Buttons." + g + ".slot"), i);
            });
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
