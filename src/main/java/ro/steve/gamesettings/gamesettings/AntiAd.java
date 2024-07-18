package ro.steve.gamesettings.gamesettings;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ro.steve.gamesettings.util.Color;
import ro.steve.gamesettings.util.SendMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiAd implements GameSettings, Listener {

    private final boolean enabled;
    private final boolean alert;
    private final boolean replace;
    private final String replacer;
    private final String notify;
    private final String bypass;
    private final Map<UUID, String> lastMessage;
    private final List<String> ads;

    public AntiAd() {
        YamlConfiguration yml = loadConfig("anti-ad");
        enabled = yml.getBoolean("enabled");
        alert = yml.getBoolean("alert");
        replace = yml.getBoolean("replace");
        replacer = yml.getString("replacer");
        notify = yml.getString("notify");
        bypass = yml.getString("bypass");
        lastMessage = new HashMap<>();
        ads = new ArrayList<>(yml.getStringList("Ads"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!enabled) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission(bypass)) {
            return;
        }
        if (lastMessage.containsKey(player.getUniqueId())) {
            if (lastMessage.get(player.getUniqueId()).equalsIgnoreCase(event.getMessage())) {
                event.setCancelled(true);
                SendMessage.sendAction(player, "no-spamming");
            }
        }
        lastMessage.put(player.getUniqueId(), event.getMessage());
        String[] split = event.getMessage().split(" ");
        AtomicBoolean isAdvertising = new AtomicBoolean(false);
        for (String m : split) {
            if (isAdvertising.get()) {
                continue;
            }
            ads.forEach(a -> {
                if (!isAdvertising.get()) {
                    if (m.contains(a)) {
                        isAdvertising.set(true);
                    }
                }
            });
        }
        if (!isAdvertising.get()) {
            isAdvertising.set(ipFind(event.getMessage()));
        }
        if (isAdvertising.get()) {
            if (alert) {
                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(notify)).forEach(p -> SendMessage.sendMessage(p, "player-try-to-advertise", "%ad%;" + event.getMessage(), "%player%;" + player.getName()));
            }
            if (replace) {
                event.setMessage(Color.process(replacer));
            }
        }
    }

    private boolean ipFind(String message) {
        String ipv4Pattern =
                "\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";

        Pattern pattern = Pattern.compile(ipv4Pattern);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

}
