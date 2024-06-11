package gg.minecrush.epicteams.api;

import gg.minecrush.epicteams.EpicTeams;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class TeamsExpansion extends PlaceholderExpansion {

    private final EpicTeams plugin;

    private final SQLite sqLite;

    public TeamsExpansion(EpicTeams plugin, SQLite sqLite) {
        this.plugin = plugin;
        this.sqLite = sqLite;
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

        if(params.equalsIgnoreCase("tag")) {
            try {
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
}
