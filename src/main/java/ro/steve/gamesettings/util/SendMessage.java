package ro.steve.gamesettings.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ro.steve.gamesettings.GameSettingsMain;
import ro.steve.gamesettings.configuration.Configuration;

public class SendMessage {

    private static final Configuration c = GameSettingsMain.getInstance().getConfiguration();

    public static void sendMessage(Player player, String path, String... replaces) {
        YamlConfiguration m = c.getConfig("messages");
        if (m.get(path) != null) {
            String message = m.getString(path);
            for (String replace : replaces) {
                String[] split = replace.split(";");
                message = message.replace(split[0], split[1]);
            }
            player.sendMessage(Color.process(message));
        }
    }

    public static void sendMessage(ConsoleCommandSender sender, String path, String... replaces) {
        YamlConfiguration m = c.getConfig("messages");
        if (m.get(path) != null) {
            String message = m.getString(path);
            for (String replace : replaces) {
                String[] split = replace.split(";");
                message = message.replace(split[0], split[1]);
            }
            sender.sendMessage(Color.process(message));
        }
    }

    public static void sendMessage(CommandSender sender, String path, String... replaces) {
        if (sender instanceof Player) {
            sendMessage((Player) sender, path, replaces);
        } else {
            sendMessage((ConsoleCommandSender) sender, path, replaces);
        }
    }

    public static void sendAction(Player player, String path, String... replaces) {
        YamlConfiguration m = c.getConfig("messages");
        if (m.get(path) != null) {
            String message = m.getString(path);
            for (String replace : replaces) {
                String[] split = replace.split(";");
                message = message.replace(split[0], split[1]);
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Color.process(message)));
        }
    }
}
