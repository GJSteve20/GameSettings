package ro.steve.gamesettings.util;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

public class Color {

    private static final List<Pattern> PATTERNS = Arrays.asList(new Solid(), new Gradient());

    public static String process(String string) {
        for (Pattern pattern : PATTERNS) {
            string = pattern.process(string);
        }

        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

    public static String replacer(String text, String finder, String replacer) {
        if (text.contains(finder)) {
            text = text.replace(finder, replacer);
        }
        return text;
    }
}
