package ro.steve.gamesettings.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;

public class Solid implements Pattern {

    java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("<#([A-Fa-f0-9]){6}>");

    @Override
    public String process(String text) {
        Matcher matcher = hexPattern.matcher(text);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = text.substring(0, matcher.start());
            final String after = text.substring(matcher.end());
            text = before + hexColor + after;
            matcher = hexPattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
