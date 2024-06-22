package gg.minecrush.epicteams.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class color {
    private static final Pattern HEX_PATTERN = Pattern.compile("<##([A-Fa-f0-9]{6})>|#([A-Fa-f0-9]{6})");

    public static String c(String message) {
        message = message.replace("%nl%", "\n");
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        int lastEnd = 0;
        while (matcher.find()) {
            buffer.append(message, lastEnd, matcher.start());
            if (matcher.group(1) != null) {
                buffer.append(hex(matcher.group(1)));
            } else if (matcher.group(2) != null) {
                buffer.append(hex(matcher.group(2)));
            }
            lastEnd = matcher.end();
        }
        buffer.append(message, lastEnd, message.length());

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static ArrayList<String> cc(String... texts) {
        ArrayList<String> a = new ArrayList<>();
        for (String text : texts)
            a.add(c(text));
        return a;
    }

    public static String noColor(String message) {
        return ChatColor.stripColor(c(message));
    }

    public static String hex(String hexCode) {
        try {
            return ChatColor.of("#" + hexCode).toString();
        } catch (Exception e) {
            return ""; // or handle the exception as needed
        }
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
