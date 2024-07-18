package ro.steve.gamesettings.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.regex.Matcher;

public class Gradient implements Pattern {

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<G#([0-9A-Fa-f]{6})>(.*?)</G#([0-9A-Fa-f]{6})>");

    @Override
    public String process(String text) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String startHex = matcher.group(1);
            String content = matcher.group(2);
            String endHex = matcher.group(3);

            Color startColor = Color.decode("#" + startHex);
            Color endColor = Color.decode("#" + endHex);

            StringBuilder formattedString = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                double ratio = (double) i / (content.length() - 1);
                Color interpolatedColor = interpolateColor(startColor, endColor, ratio);
                formattedString.append(ChatColor.of(interpolatedColor)).append(content.charAt(i));
            }
            text = text.replaceFirst("<G#([0-9A-Fa-f]{6})>(.*?)</G#([0-9A-Fa-f]{6})>", formattedString.toString());
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private Color interpolateColor(Color start, Color end, double ratio) {
        int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
        int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }
}
