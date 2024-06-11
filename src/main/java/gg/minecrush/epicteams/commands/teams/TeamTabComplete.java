package gg.minecrush.epicteams.commands.teams;

import gg.minecrush.epicteams.EpicTeams;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Config;
import gg.minecrush.epicteams.database.yml.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamTabComplete implements TabCompleter {

    private final SQLite sqLite;
    private final Config config;

    public TeamTabComplete(SQLite sqLite, Config config) {
        this.sqLite = sqLite;
        this.config = config;
    }

    private final List<String> playerCache = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<String>();
        if (args.length == 1) {
            list.add("create");
            list.add("disband");
            list.add("invite");
            list.add("join");
            list.add("leave");
            list.add("who");
            list.add("info");
            list.add("demote");
            list.add("promote");
            list.add("transfer");
            list.add("chat");
            if (config.getValueBoolean("team-homes")){
                list.add("home");
                list.add("sethome");
            }
            return list;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                list.add("<name>");
            }

            if (args[0].equalsIgnoreCase("join")) {
                Player player = (Player) sender;
                try {
                    List<String> invites = sqLite.getInvites(player.getUniqueId());
                    if (!invites.isEmpty()) {
                        list.addAll(invites);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (args[0].equalsIgnoreCase("invite")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }
            }

            if (args[0].equalsIgnoreCase("promote")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }
            }

            if (args[0].equalsIgnoreCase("demote")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }

            }

            if (args[0].equalsIgnoreCase("who")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }

            }
            return list;
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                list.add("<tag>");
            }
            return list;
        }

        return null;
    }
}
