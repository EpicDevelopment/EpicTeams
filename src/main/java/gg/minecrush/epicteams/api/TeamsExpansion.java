package gg.minecrush.epicteams.api;

import gg.minecrush.epicteams.EpicTeams;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class TeamsExpansion extends PlaceholderExpansion {

    private final EpicTeams plugin;

    private final SQLite sqLite;

    private final Messages messages;

    public TeamsExpansion(EpicTeams plugin, SQLite sqLite, Messages messages) {
        this.plugin = plugin;
        this.sqLite = sqLite;
        this.messages = messages;
    }

    @Override
    public String getAuthor() {
        return "awel & snowyjs";
    }
    @Override
    public String getIdentifier() {
        return "epicteams";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("team")) {
            try {
                if (!sqLite.playerExists(player.getName())){
                    return "";
                }
                String team = sqLite.getTeam(player);
                if(team != null) {
                    return team;
                } else {
                    return "";
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }

        if(params.equalsIgnoreCase("prefix")) {
            try {
                if (!sqLite.playerExists(player.getName())){
                    return "";
                }
                String team = sqLite.getTeam(player);
                if(team != null) {
                    return messages.getReplacedMessage("placeholder-prefix").replace("{TEAM}", team).replace("{UPPERCASE_TEAM}", team.toUpperCase().replace("{PROPERCASE_TEAM}", capitalizeFirstLetter(team)));
                } else {
                    return "";
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }

        if(params.equalsIgnoreCase("tag")) {
            try {
                if (!sqLite.playerExists(player.getName())){
                    return "";
                }
                String team = sqLite.getTeamDisplayName(sqLite.getTeam(player));
                if(team != null) {
                    return team;
                } else {
                    return "";
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        if(params.equalsIgnoreCase("kills")) {
            try {
                if (!sqLite.playerExists(player.getName())){
                    return "";
                }
                if (sqLite.teamExists(sqLite.getTeam(player))) {
                    int team = sqLite.getTeamKills(sqLite.getTeam(player));
                    if (team > 0) {
                        return "" + team;
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }

        if(params.equalsIgnoreCase("deaths")) {
            try {
                if (!sqLite.playerExists(player.getName())){
                    return "";
                }
                if (sqLite.teamExists(sqLite.getTeam(player))){
                    int team = sqLite.getTeamDeaths(sqLite.getTeam(player));
                    if(team > 0) {
                        return "" + team;
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
