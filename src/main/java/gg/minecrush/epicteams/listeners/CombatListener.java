package gg.minecrush.epicteams.listeners;

import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.sql.SQLException;

public class CombatListener implements Listener {

    private final SQLite sqLite;

    public CombatListener(SQLite sqLite) {
        this.sqLite = sqLite;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        OfflinePlayer player = event.getEntity();
        OfflinePlayer killer = null;
        if (event.getEntity().getKiller() != null && event.getEntity().getKiller() instanceof Player) {
            killer = (OfflinePlayer) event.getEntity().getKiller();
        }
        try {
            if (player != null) {
                String team = sqLite.getTeam(player);
                if (team != null) {
                    sqLite.updateTeamDeaths(team, sqLite.getTeamDeaths(team) + 1);
                }
            }
            if (killer != null) {
                String team = sqLite.getTeam(killer);
                if (team != null) {
                    sqLite.updateTeamKills(team, sqLite.getTeamKills(team) + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
