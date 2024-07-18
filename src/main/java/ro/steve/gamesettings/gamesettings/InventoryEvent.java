package ro.steve.gamesettings.gamesettings;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.inventory.GameSettingsInventory;
import ro.steve.gamesettings.item.ItemBuilder;
import ro.steve.gamesettings.util.Color;
import ro.steve.gamesettings.util.SendMessage;

public class InventoryEvent implements GameSettings, Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof GameSettingsInventory)) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        event.setResult(Event.Result.DENY);
        if (event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("buy"), PersistentDataType.STRING)) {
            String tier = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("buy"), PersistentDataType.STRING);
            YamlConfiguration yml = GameSettingsMain.getInstance().getConfiguration().getConfig("gens");
            double money = yml.getDouble("Gens." + tier + ".upgrade_cost");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getWhoClicked().getUniqueId());
            if (event.getWhoClicked().getInventory().firstEmpty() == -1) {
                SendMessage.sendAction((Player) event.getWhoClicked(), "full-inventory-buy");
                event.getWhoClicked().closeInventory();
                return;
            }
            if (GameSettingsMain.getEconomy().has(offlinePlayer, money)) {
                GameSettingsMain.getEconomy().withdrawPlayer(offlinePlayer, money);
                event.getWhoClicked().getInventory().addItem(new ItemBuilder()
                        .type(Material.valueOf(yml.getString("Gens." + tier + ".material")))
                        .amount(1)
                        .name(Color.process(yml.getString("Gens." + tier + ".name")))
                        .glowing(true)
                        .withPersistent("gens.tier", PersistentDataType.STRING, tier)
                        .build());
                SendMessage.sendAction((Player) event.getWhoClicked(), "gen-buy", "%money%;" + money);
            }
            event.getWhoClicked().closeInventory();
        }
        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("action"), PersistentDataType.STRING)) {
            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("action"), PersistentDataType.STRING).equalsIgnoreCase("confirm")) {
                String[] split = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("location"), PersistentDataType.STRING).split(";");
                Location loc = new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                String tier = GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().get(loc);
                String next = "Tier_" + (Integer.parseInt(tier.substring(5)) + 1);
                YamlConfiguration yml = GameSettingsMain.getInstance().getConfiguration().getConfig("gens");
                double money = yml.getDouble("Gens." + next + ".upgrade_cost");
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getWhoClicked().getUniqueId());
                if (!GameSettingsMain.getEconomy().has(offlinePlayer, money)) {
                    SendMessage.sendAction((Player) event.getWhoClicked(), "not-enough-money", "%money%;" + money);
                    return;
                }
                event.getWhoClicked().closeInventory();
                loc.getBlock().setType(Material.valueOf(yml.getString("Gens." + next + ".material")));
                GameSettingsMain.getInstance().getGameSettingsStorage().getEventBlocks().put(loc, next);
                SendMessage.sendAction((Player) event.getWhoClicked(), "gen-upgrade", "%money%;" + money);
                GameSettingsMain.getEconomy().withdrawPlayer(offlinePlayer, money);
            } else {
                event.getWhoClicked().closeInventory();
            }
        }
    }
}
