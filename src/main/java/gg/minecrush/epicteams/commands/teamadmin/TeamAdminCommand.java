package gg.minecrush.epicteams.commands.teamadmin;

import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Config;
import gg.minecrush.epicteams.database.yml.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TeamAdminCommand implements CommandExecutor {

    private final SQLite sqLite;
    private final Messages messages;
    private final Config config;
    private final Plugin plugin;

    public TeamAdminCommand(SQLite sqLite, Messages messages, Config config, Plugin plugin) {
        this.sqLite = sqLite;
        this.messages = messages;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand = args[0].toLowerCase();
        Player player = (Player) sender;
        if (player.hasPermission(config.getValue("admin-permission"))){
            switch (subCommand) {
                case "delete":
                    handledeleteTeamCommand(player, args);
                    break;
                case "reload":
                    handlereloadCommand(player, args);
                    break;
                case "help":
                    handleHelpCommand(player);
                    break;
                default:
                    player.sendMessage(messages.getReplacedMessage("invalid-admin-argument"));
                    break;
            }
        } else {
            player.sendMessage(messages.getReplacedMessage("no-permission"));
        }
        return true;
    }

    public void handlereloadCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(messages.getReplacedMessage("invalid-admin-argument"));
            return;
        }

        long startTime = System.currentTimeMillis();

        config.reloadConfig();
        messages.reloadConfig();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        sender.sendMessage(messages.getReplacedMessage("reloaded").replace("%time%", Long.toString(duration)));
    }

    private void handleHelpCommand(Player player){
        String teamHelpmessage = messages.getReplacedMessage("adminteam-help.message");
        player.sendMessage(teamHelpmessage);
    }

    public void handledeleteTeamCommand(Player player, String[] args){
        if (args.length != 2){
            player.sendMessage(messages.getReplacedMessage("invalid-admin-argument"));
            return;
        }
        try {
            String team = args[1];
            if (sqLite.teamExists(team)){
                player.sendMessage(messages.getReplacedMessage("team-deleted").replace("%tag%", sqLite.getTeamDisplayName(team)).replace("%name%", team));
                sqLite.deleteTeam(team);
            } else {
                player.sendMessage(messages.getReplacedMessage("team-no-exist"));
            }
        } catch (Exception e){
            player.sendMessage(messages.getReplacedMessage("team-not-found"));
        }
    }
}
