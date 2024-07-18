package ro.steve.gamesettings.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.inventory.GameSettingsInventory;
import ro.steve.gamesettings.item.ItemBuilder;
import ro.steve.gamesettings.util.Color;
import ro.steve.gamesettings.util.SendMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GameSettingsCommands implements CommandExecutor {

    public GameSettingsCommands(Plugin plugin, String... commands) {
        for (String command : commands) {
            plugin.getServer().getPluginCommand(command).setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@Nullable CommandSender sender,@Nullable Command cmd,@Nullable String label,@Nullable String[] args) {
        if (cmd == null) {
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("gens")) {
            if (sender instanceof Player) {
                ((Player) sender).openInventory(new GameSettingsInventory("buy", null).getInventory());
            } else {
                SendMessage.sendMessage(sender, "in-game-only");
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("sellall")) {
            if (sender instanceof Player player) {
                AtomicReference<Double> money = new AtomicReference<>((double) 0);
                AtomicInteger stacks = new AtomicInteger();
                YamlConfiguration y = GameSettingsMain.getInstance().getConfiguration().getConfig("gens");
                y.getConfigurationSection("Gens").getKeys(false).forEach(b -> {
                    List<String> lore = new ArrayList<>();
                    if (y.get("Gens." + b + ".item.lore") != null) {
                        y.getStringList("Gens." + b + ".item.lore").forEach(m -> lore.add(Color.process(m)));
                    }
                    ItemStack i = new ItemBuilder()
                            .type(Material.valueOf(y.getString("Gens." + b + ".item.material")))
                            .name(Color.process(y.getString("Gens." + b + ".item.name")))
                            .lore(lore)
                            .build();
                    for (ItemStack is : player.getInventory().getContents()) {
                        if (is == null) {
                            continue;
                        }
                        if (is.isSimilar(i)) {
                            money.set(money.get() + (is.getAmount() * y.getDouble("Gens." + b + ".item.cost")));
                            stacks.set(stacks.get() + is.getAmount());
                        }
                    }
                    i.setAmount(2400);
                    player.getInventory().removeItem(i);
                });
                if (stacks.get() == 0) {
                    SendMessage.sendAction(player, "no-items");
                } else {
                    SendMessage.sendAction(player, "sold", "%items%;" + stacks.get(), "%money%;" + money);
                    GameSettingsMain.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), money.get());
                }
            } else {
                SendMessage.sendMessage(sender, "in-game-only");
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("clearchat")) {
            YamlConfiguration y = GameSettingsMain.getInstance().getConfiguration().getConfig("clearchat");
            if (y.getBoolean("enabled")) {
                if (!sender.hasPermission(y.getString("permission"))) {
                    SendMessage.sendMessage(sender, "no-permission");
                    return false;
                }
                for (int i = 0; i < y.getInt("times"); i++) {
                    Bukkit.broadcastMessage(" ");
                }
                Bukkit.broadcastMessage(Color.process(GameSettingsMain.getInstance().getConfiguration().getConfig("messages").getString("clear-chat").replace("%player%", sender.getName())));
            } else {
                SendMessage.sendMessage(sender, "not-enabled", "%feature%;clearchat");
                return false;
            }
        }
        return false;
    }
}
