package gg.minecrush.epicteams.listeners;

import gg.minecrush.epicteams.database.sqlite.SQLite;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final SQLite sqLite;

    public JoinListener(SQLite sqLite) {
        this.sqLite = sqLite;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            if (!sqLite.playerExists(player.getUniqueId().toString())) {
                sqLite.registerPlayer(player);

                if (!sqLite.getName(player).equals(player.getDisplayName())){
                    sqLite.updatePlayername(player, player.getDisplayName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
