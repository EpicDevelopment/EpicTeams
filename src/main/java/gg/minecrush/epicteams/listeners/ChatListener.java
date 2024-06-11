package gg.minecrush.epicteams.listeners;

import gg.minecrush.epicteams.EpicTeams;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Messages;
import gg.minecrush.epicteams.util.color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class ChatListener implements Listener {

    private final SQLite sqLite;

    private final Messages messages;

    private final EpicTeams epicTeams;

    public ChatListener(SQLite sqLite, Messages messages, EpicTeams epicTeams) {
        this.sqLite = sqLite;
        this.messages = messages;
        this.epicTeams = epicTeams;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String message = e.getMessage();
        try {
            if (sqLite.getTeamchat(p).equals(true)){
                if (!sqLite.getTeam(p).isEmpty()){
                    e.setCancelled(true);
                    sqLite.teamAnnounce(sqLite.getTeam(p), messages.getReplacedMessage("team-chat").replace("%player%", p.getDisplayName()).replace("%message%", message).replace("%prefix%", color.c(replaceHexColors(epicTeams.getPlayerPrefix(p)))));
                } else {
                    sqLite.updatePlayerteamchat(p, false);
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    private String replaceHexColors(String message) {
        String hexPattern = "#([A-Fa-f0-9]{6})";
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(hexPattern).matcher(message);
        while (matcher.find()) {
            builder.append(message, lastIndex, matcher.start());
            String hexColor = matcher.group(1);
            builder.append(convertHexToMinecraftColor(hexColor));
            lastIndex = matcher.end();
        }
        builder.append(message.substring(lastIndex));
        return builder.toString();
    }

    private String convertHexToMinecraftColor(String hexColor) {
        // Minecraft format for hex colors uses §x§R§R§G§G§B§B
        StringBuilder minecraftColor = new StringBuilder("§x");
        for (char c : hexColor.toCharArray()) {
            minecraftColor.append('§').append(c);
        }
        return minecraftColor.toString();
    }

}
