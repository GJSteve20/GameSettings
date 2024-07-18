package ro.steve.gamesettings.gamesettings;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.util.Color;
import ro.steve.gamesettings.util.SendMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AntiSwear implements GameSettings, Listener {

    private final boolean enabled;
    private final List<String> swears;
    private final String bypass;
    private final String notify;
    private final boolean alert;
    private final boolean censored;
    private final boolean auto_mod;
    private final int actions;
    private final String command;
    private final Map<UUID, Integer> numberOfActions;

    public AntiSwear() {
        YamlConfiguration yml = loadConfig("anti-swear");
        enabled = yml.getBoolean("enabled");
        swears = new ArrayList<>(yml.getStringList("Swears"));
        bypass = yml.getString("bypass");
        notify = yml.getString("notify");
        alert = yml.getBoolean("alert");
        censored = yml.getBoolean("censored");
        auto_mod = yml.getBoolean("auto-mod");
        actions = yml.getInt("actions");
        command = yml.getString("mute-command");
        numberOfActions = new HashMap<>();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!enabled) {
            return;
        }
        Player player = event.getPlayer();
        if (censored) {
            String censored = "**";
            String[] split = event.getMessage().split(" ");
            for (String m : split) {
                if (m.contains(censored)) {
                    if (event.getPlayer().hasPermission(bypass)) {
                        return;
                    }
                    event.setCancelled(true);
                    SendMessage.sendAction(player, "no-swearing");
                    if (auto_mod) {
                        if (!numberOfActions.containsKey(player.getUniqueId())) {
                            numberOfActions.put(player.getUniqueId(), 1);
                        } else {
                            numberOfActions.put(player.getUniqueId(), numberOfActions.get(player.getUniqueId()) + 1);
                        }
                        if (numberOfActions.get(player.getUniqueId()) >= actions) {
                            String command = this.command.replace("%player%", player.getName());
                            Bukkit.getScheduler().runTaskLater(GameSettingsMain.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Color.process(command)), 5L);
                            numberOfActions.remove(player.getUniqueId());
                        }
                    }
                }
            }
        }
        AtomicBoolean isSwearing = new AtomicBoolean(false);
        swears.forEach(s -> {
            if (event.getMessage().contains(s)) {
                if (isSwearing.get()) {
                    return;
                }
                isSwearing.set(true);
            }
        });
        if (isSwearing.get()) {
            if (event.getPlayer().hasPermission(bypass)) {
                return;
            }
            event.setCancelled(true);
            if (alert) {
                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(notify)).forEach(p -> SendMessage.sendMessage(p, "player-try-to-swear", "%player%;" + player.getName(), "%message%;" + event.getMessage()));
            }
            SendMessage.sendAction(player, "no-swearing");
            if (auto_mod) {
                if (!numberOfActions.containsKey(player.getUniqueId())) {
                    numberOfActions.put(player.getUniqueId(), 1);
                } else {
                    numberOfActions.put(player.getUniqueId(), numberOfActions.get(player.getUniqueId()) + 1);
                }
                if (numberOfActions.get(player.getUniqueId()) >= actions) {
                    String command = this.command.replace("%player%", player.getName());
                    Bukkit.getScheduler().runTaskLater(GameSettingsMain.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Color.process(command)), 5L);
                    numberOfActions.remove(player.getUniqueId());
                }
            }
        }
    }
}
