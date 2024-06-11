package gg.minecrush.epicteams.commands.teams;

import gg.minecrush.epicteams.EpicTeams;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Config;
import gg.minecrush.epicteams.database.yml.Messages;
import gg.minecrush.epicteams.util.color;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

public class TeamCommand implements CommandExecutor {

    private final SQLite sqLite;
    private final Messages messages;
    private final Config config;
    private final Plugin plugin;
    private final EpicTeams epicTeams;

    public TeamCommand(SQLite sqLite, Messages messages, Config config, Plugin plugin, EpicTeams epicTeams) {
        this.sqLite = sqLite;
        this.messages = messages;
        this.config = config;
        this.plugin = plugin;
        this.epicTeams = epicTeams;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return false;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "create":
                    handleCreateCommand(player, args);
                    break;
                case "disband":
                    handleDisbandCommand(player, args);
                    break;
                case "info":
                    handleInfoCommand(player, args);
                    break;
                case "promote":
                    handlePromoteCommand(player, args);
                    break;
                case "demote":
                    handleDemoteCommand(player, args);
                    break;
                case "chat":
                    handleChatCommand(player, args);
                    break;
                case "leave":
                    handleLeaveCommand(player);
                    break;
                case "settings":
                    handleSettingsCommand(player, args);
                    break;
                case "transfer":
                    handleTransferCommand(player, args);
                    break;
                case "who":
                    handleWhoCommand(player, args);
                    break;
                case "invite":
                    handleInviteCommand(player, args);
                    break;
                case "join":
                    handleAcceptCommand(player, args);
                    break;
                case "help":
                    handleHelpCommand(player);
                    break;
                case "kick":
                    handleKickCommand(player, args);
                    break;
                case "home":
                    if (config.getValueBoolean("team-homes")){
                        handleHomeCommand(player);
                    } else {
                        sender.sendMessage(messages.getReplacedMessage("invalid-argument"));
                    }
                    break;
                case "sethome":
                    if (config.getValueBoolean("team-homes")) {
                        handleSetHomeCommand(player);
                    } else {
                        sender.sendMessage(messages.getReplacedMessage("invalid-argument"));
                    }
                    break;
                default:
                    handleHelpCommand(player);
                    break;
            }
        } else {
            sender.sendMessage(color.c("&c[EpicTeams] Console cannot execute this command."));
        }
        return true;
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        String name = args[1].toLowerCase();
        String displayName = args[2];
        List<String> bannedNames = config.getBannedNames();

        if (bannedNames.contains(name.toLowerCase()) || bannedNames.contains(displayName.toLowerCase())) {
            player.sendMessage(messages.getReplacedMessage("banned-name"));
            return;
        }

        if (!name.matches("[a-zA-Z]+") || !displayName.matches("[a-zA-Z]+")) {
            player.sendMessage(messages.getReplacedMessage("invalid-characters"));
            return;
        }

        if (displayName.length() > config.getValueInt("team-tag-limit") || displayName.length() < config.getValueInt("team-tag-shortest")) {
            player.sendMessage(messages.getReplacedMessage("tag-limit").replace("%limit-longest%", config.getValue("team-tag-limit")).replace("%limit-shortest%", config.getValue("team-tag-shortest")));
            return;
        }

        if (displayName.length() > config.getValueInt("team-name-limit") || displayName.length() < config.getValueInt("team-name-shortest")) {
            player.sendMessage(messages.getReplacedMessage("name-limit").replace("%limit-longest%", config.getValue("team-name-limit")).replace("%limit-shortest%", config.getValue("team-name-shortest")));
            return;
        }

        try {
            if (sqLite.teamExists(name)) {
                player.sendMessage(messages.getReplacedMessage("team-exist"));
            } else {
                if (!sqLite.getTeam(player).isBlank()) {
                    player.sendMessage(messages.getReplacedMessage("already-in-team"));
                    return;
                }

                sqLite.registerTeam(name, displayName, player.getUniqueId().toString());
                sqLite.updatePlayerteam(player, name);
                player.sendMessage(messages.getReplacedMessage("team-created").replace("%tag%", displayName).replace("%name%", name));
                if (config.getValueBoolean("broadcast-team-creation")) {
                    Bukkit.broadcastMessage(messages.getReplacedMessage("team-creation").replace("%tag%", displayName).replace("%name%", name).replace("%player%", player.getDisplayName()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDisbandCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            String name = sqLite.getTeam(player);
            if (name == null) {
                player.sendMessage(messages.getReplacedMessage("team-no-exist"));
            } else {
                String displayName = sqLite.getTeamDisplayName(name);
                if (sqLite.teamExists(name)) {
                    if (sqLite.getTeamOwner(name).equals(player.getUniqueId().toString())) {
                        sqLite.deleteTeam(name);
                        player.sendMessage(messages.getReplacedMessage("team-deleted").replace("%tag%", displayName).replace("%name%", name).replace("%player%", player.getDisplayName()));
                        if (config.getValueBoolean("broadcast-team-deletion")) {
                            Bukkit.broadcastMessage(messages.getReplacedMessage("team-deletion").replace("%tag%", displayName).replace("%name%", name).replace("%player%", player.getDisplayName()));
                        }
                    } else {
                        player.sendMessage(messages.getReplacedMessage("team-not-owner"));
                    }
                } else {
                    player.sendMessage(messages.getReplacedMessage("team-no-exist"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleInfoCommand(Player player, String[] args) {
        try {
            String team;
            if (args.length < 2) {
                team = sqLite.getTeam(player);
                if (team.isBlank()) {
                    player.sendMessage(messages.getReplacedMessage("not-in-team"));
                    return;
                }
            } else {
                team = args[1];
            }

            if (sqLite.teamExists(team)) {
                UUID ownerUUID = UUID.fromString(sqLite.getTeamOwner(team));
                String owner = sqLite.getNames(ownerUUID);
                int kills = sqLite.getTeamKills(team);
                int deaths = sqLite.getTeamDeaths(team);
                List<UUID> mods = sqLite.getTeamModerators(team);
                List<UUID> members = sqLite.getTeamMembers(team);
                List<String> modNames = new ArrayList<>();
                for (UUID mod : mods) {
                    modNames.add(sqLite.getNames(mod));
                }
                List<String> memberNames = new ArrayList<>();
                for (UUID member : members) {
                    memberNames.add(sqLite.getNames(member));
                }
                String teamInfoMessage = messages.getReplacedMessage("team-info.message")
                        .replace("%team_name%", team)
                        .replace("%team_owner%", owner)
                        .replace("%team_kills%", String.valueOf(kills))
                        .replace("%team_deaths%", String.valueOf(deaths))
                        .replace("%team_mods%", String.join(", ", modNames))
                        .replace("%team_members%", String.join(", ", memberNames))
                        .replace("%tag%", sqLite.getTeamDisplayName(team));
                player.sendMessage(teamInfoMessage);
            } else {
                player.sendMessage(messages.getReplacedMessage("team-no-exist"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handlePromoteCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            String team = sqLite.getTeam(player);

            if (team == null){
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (sqLite.getTeam(target).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }

            if (!team.equals(sqLite.getTeam(target))) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }
            if (!sqLite.getTeamOwner(team).equals(player.getUniqueId().toString())) {
                player.sendMessage(messages.getReplacedMessage("team-not-owner"));
                return;
            }

            if (target.getName().equals(player.getName())){
                player.sendMessage(messages.getReplacedMessage("cant-edit-player"));
                return;
            }

            List<UUID> mods = sqLite.getTeamModerators(team);
            if (mods.contains(target.getUniqueId())) {
                player.sendMessage(messages.getReplacedMessage("already-a-mod"));
                return;
            } else {
                sqLite.addTeamModerator(team, target.getUniqueId());
                sqLite.removeTeamMember(team, target.getUniqueId());
                sqLite.teamAnnounce(team, messages.getReplacedMessage("player-promoted").replace("%target%", target.getName()).replace("%name%", team).replace("%player%", player.getDisplayName()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDemoteCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            String team = sqLite.getTeam(player);

            if (team == null){
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (sqLite.getTeam(target).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }

            if (!team.equals(sqLite.getTeam(target))) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }
            if (!sqLite.getTeamOwner(team).equals(player.getUniqueId().toString())) {
                player.sendMessage(messages.getReplacedMessage("team-not-owner"));
                return;
            }

            if (target.getName().equals(player.getName())){
                player.sendMessage(messages.getReplacedMessage("cant-edit-player"));
                return;
            }

            List<UUID> members = sqLite.getTeamMembers(team);
            if (members.contains(target.getUniqueId())) {
                player.sendMessage(messages.getReplacedMessage("already-a-member"));
                return;
            } else {
                sqLite.removeTeamModerator(team, target.getUniqueId());
                sqLite.addTeamMember(team, target.getUniqueId());
                sqLite.teamAnnounce(team, messages.getReplacedMessage("player-demoted").replace("%target%", target.getName()).replace("%name%", team).replace("%player%", player.getName()).replace("%team%", team));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleKickCommand(Player player, String[] args){
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            String team = sqLite.getTeam(player);

            if (team == null) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (sqLite.getTeam(target).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }

            if (!team.equals(sqLite.getTeam(target))) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }

            if (target.getName().equals(player.getName())){
                player.sendMessage(messages.getReplacedMessage("cant-kick-player"));
                return;
            }

            if (!sqLite.getTeamOwner(team).equals(player.getUniqueId().toString())) {
                List<UUID> mods = sqLite.getTeamModerators(team);
                if (mods.contains(target.getUniqueId())){
                    player.sendMessage(messages.getReplacedMessage("team-not-owner"));
                    return;
                }

                if (target.getUniqueId().toString().equals(sqLite.getTeamOwner(team))) {
                    player.sendMessage(messages.getReplacedMessage("cant-kick-player"));
                    return;
                }

                if (!mods.contains(player.getUniqueId())){
                    player.sendMessage(messages.getReplacedMessage("team-not-moderator"));
                    return;
                }
            }

            List<UUID> mods = sqLite.getTeamModerators(team);
            if (mods.contains(target.getUniqueId())){
                sqLite.removeTeamModerator(team, target.getUniqueId());
            } else {
                sqLite.removeTeamMember(team, target.getUniqueId());
            }

            sqLite.updatePlayerteamchat(target, false);
            sqLite.updatePlayerteam(target, "");
            sqLite.teamAnnounce(team, messages.getReplacedMessage("player-kicked").replace("%target%", target.getName()).replace("%player%", player.getDisplayName()));



        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void handleChatCommand(Player player, String[] args) {
        try {
            String playerTeam = sqLite.getTeam(player);
            if (playerTeam == null || playerTeam.isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
            } else {
                if (args.length > 1) {
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    sqLite.teamAnnounce(playerTeam, messages.getReplacedMessage("team-chat")
                            .replace("%player%", player.getDisplayName())
                            .replace("%message%", message)
                            .replace("%prefix%", color.c(replaceHexColors(epicTeams.getPlayerPrefix(player)))));
                } else {
                    if (!sqLite.getTeamchat(player)) {
                        sqLite.updatePlayerteamchat(player, true);
                        player.sendMessage(messages.getReplacedMessage("team-chat-toggle-enable"));
                    } else {
                        sqLite.updatePlayerteamchat(player, false);
                        player.sendMessage(messages.getReplacedMessage("team-chat-toggle-disable"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLeaveCommand(Player player) {
        try {
            String playerTeam = sqLite.getTeam(player);
            if (playerTeam.isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (sqLite.getTeamOwner(playerTeam).equals(player.getUniqueId().toString())) {
                player.sendMessage(messages.getReplacedMessage("disband-first"));
                return;
            }

            sqLite.updatePlayerteamchat(player, false);
            sqLite.updatePlayerteam(player, "");

            List<UUID> mods = sqLite.getTeamModerators(playerTeam);

            sqLite.removeTeamMember(playerTeam, player.getUniqueId());
            if (mods.contains(player.getUniqueId())) {
                sqLite.removeTeamModerator(playerTeam, player.getUniqueId());
            }
            player.sendMessage(messages.getReplacedMessage("left-team").replace("%name%", playerTeam).replace("%player%", player.getDisplayName()));
            sqLite.teamAnnounce(playerTeam, messages.getReplacedMessage("player-left").replace("%name%", playerTeam).replace("%player%", player.getDisplayName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSettingsCommand(Player player, String[] args) {

    }

    private void handleTransferCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(messages.getReplacedMessage("not-online"));
                return;
            }

            String team = sqLite.getTeam(player);

            if (team == null){
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (sqLite.getTeam(target).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }

            if (target.getName().equals(player.getName())){
                player.sendMessage(messages.getReplacedMessage("cant-edit-player"));
                return;
            }

            if (!team.equals(sqLite.getTeam(target))) {
                player.sendMessage(messages.getReplacedMessage("target-not-in-team").replace("%target%", target.getName()));
                return;
            }
            if (!sqLite.getTeamOwner(team).equals(player.getUniqueId().toString())) {
                player.sendMessage(messages.getReplacedMessage("team-not-owner"));
                return;
            }

            List<UUID> members = sqLite.getTeamMembers(team);
            if (members.contains(target.getUniqueId())) {
                sqLite.removeTeamMember(team, target.getUniqueId());
            }

            List<UUID> mods = sqLite.getTeamMembers(team);
            if (mods.contains(target.getUniqueId())) {
                sqLite.removeTeamModerator(team, target.getUniqueId());
            }
            sqLite.addTeamModerator(team, player.getUniqueId());
            sqLite.updateTeamOwner(team, target.getUniqueId().toString());
            sqLite.teamAnnounce(team, messages.getReplacedMessage("team-transfered").replace("%player%", player.getDisplayName()).replace("%target%", target.getDisplayName()));


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void handleWhoCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        try {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                return;
            }

            if (sqLite.getTeam(target).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            String team = sqLite.getTeam(target);
            UUID ownerUUID = UUID.fromString(sqLite.getTeamOwner(team));
            String owner = sqLite.getNames(ownerUUID);
            int kills = sqLite.getTeamKills(team);
            int deaths = sqLite.getTeamDeaths(team);
            List<UUID> mods = sqLite.getTeamModerators(team);
            List<UUID> members = sqLite.getTeamMembers(team);
            List<String> modNames = new ArrayList<>();
            for (UUID mod : mods) {
                modNames.add(sqLite.getNames(mod));
            }
            List<String> memberNames = new ArrayList<>();
            for (UUID member : members) {
                memberNames.add(sqLite.getNames(member));
            }
            String teamInfoMessage = messages.getReplacedMessage("team-info.message")
                    .replace("%team_name%", team)
                    .replace("%team_owner%", owner)
                    .replace("%team_kills%", String.valueOf(kills))
                    .replace("%team_deaths%", String.valueOf(deaths))
                    .replace("%team_mods%", String.join(", ", modNames))
                    .replace("%team_members%", String.join(", ", memberNames))
                    .replace("%tag%", sqLite.getTeamDisplayName(team));
            player.sendMessage(teamInfoMessage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleInviteCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }

        Player invitedPlayer = Bukkit.getPlayer(args[1]);
        if (invitedPlayer == null || !invitedPlayer.isOnline()) {
            player.sendMessage(messages.getReplacedMessage("not-online"));
            return;
        }

        try {
            String teamName = sqLite.getTeam(player);
            if (teamName.isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            List<UUID> mods = sqLite.getTeamModerators(teamName);
            if (!sqLite.getTeamOwner(teamName).equals(player.getUniqueId().toString()) && !mods.contains(player.getUniqueId())) {
                player.sendMessage(messages.getReplacedMessage("team-not-moderator"));
                return;
            }

            if (!sqLite.getTeam(invitedPlayer).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("target-in-team"));
                return;
            }

            if (!sqLite.hasInvite(invitedPlayer.getUniqueId(), teamName)) {
                sqLite.addInvite(invitedPlayer.getUniqueId(), teamName);
                invitedPlayer.sendMessage(messages.getReplacedMessage("invited-player").replace("%player%", invitedPlayer.getName()).replace("%name%", teamName).replace("%tag%", sqLite.getTeamDisplayName(teamName)));
                sqLite.teamAnnounce(teamName, messages.getReplacedMessage("player-invited").replace("%target%", invitedPlayer.getDisplayName()).replace("player", player.getDisplayName()));
            } else {
                player.sendMessage(messages.getReplacedMessage("already-invited"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAcceptCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.getReplacedMessage("invalid-argument"));
            return;
        }
        try {
            if (!sqLite.getTeam(player).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("already-in-team"));
                return;
            }

            String teamName = args[1].toLowerCase();
            if (sqLite.hasInvite(player.getUniqueId(), teamName)) {
                sqLite.removeInvite(player.getUniqueId(), teamName);
                sqLite.addTeamMember(teamName, player.getUniqueId());
                sqLite.updatePlayerteam(player, teamName);
                sqLite.teamAnnounce(teamName, messages.getReplacedMessage("player-joined").replace("%player%", player.getName()));
            } else {
                player.sendMessage(messages.getReplacedMessage("not-invited"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleHelpCommand(Player player){
        String teamHelpmessage = messages.getReplacedMessage("team-help.message");
        player.sendMessage(teamHelpmessage);
    }

    public void handleSetHomeCommand(Player player){
        try {
            if (sqLite.getTeam(player).isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            if (!sqLite.getTeamOwner(sqLite.getTeam(player)).equals(player.getUniqueId().toString())) {
                List<UUID> mods = sqLite.getTeamModerators(sqLite.getTeam(player));
                if (!mods.contains(player.getUniqueId())) {
                    player.sendMessage(messages.getReplacedMessage("team-not-moderator"));
                    return;
                }
            }

            Location loc = player.getLocation();
            String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
            sqLite.updateTeamHome(sqLite.getTeam(player), location);
            String worldName = player.getWorld().getName();
            double x = Math.round(player.getLocation().getX());
            double y = Math.round(player.getLocation().getY());
            double z = Math.round(player.getLocation().getZ());

            String locs = x + "," + y + "," + z + " in " + worldName;
            sqLite.teamAnnounce(sqLite.getTeam(player), messages.getReplacedMessage("team-home-updated").replace("%location%", locs).replace("%player%", player.getDisplayName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleHomeCommand(Player player) {
        try {
            String team = sqLite.getTeam(player);
            if (team.isBlank()) {
                player.sendMessage(messages.getReplacedMessage("not-in-team"));
                return;
            }

            Location initialLocation = player.getLocation();
            if (config.getValueBoolean("teleport-wait")){
                player.sendMessage(messages.getReplacedMessage("teleporting-player").replace("%time%", config.getValue("teleport-time")));
                Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (player.getLocation().distance(initialLocation) < 1) {
                            try {
                                String home = sqLite.getTeamHome(team);
                                if (!home.isBlank()) {
                                    String[] parts = home.split(",");
                                    Location homeLocation = new Location(
                                            Bukkit.getWorld(parts[0]),
                                            Double.parseDouble(parts[1]),
                                            Double.parseDouble(parts[2]),
                                            Double.parseDouble(parts[3]),
                                            Float.parseFloat(parts[4]),
                                            Float.parseFloat(parts[5])
                                    );
                                    player.teleport(homeLocation);
                                    player.sendMessage(messages.getReplacedMessage("teleported-home"));
                                } else {
                                    player.sendMessage(messages.getReplacedMessage("no-home"));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                player.sendMessage("An error occurred while fetching the home location.");
                            }
                        } else {
                            player.sendMessage(messages.getReplacedMessage("canceled-move"));
                        }
                    }
                }, 20L * config.getValueInt("teleport-time")); // 60L = 3 seconds (20 ticks per second)
            } else {
                try {
                    String home = sqLite.getTeamHome(team);
                    if (!home.isBlank()) {
                        if (player.getLocation().distance(initialLocation) > 1) {
                            String[] parts = home.split(",");
                            Location homeLocation = new Location(
                                    Bukkit.getWorld(parts[0]),
                                    Double.parseDouble(parts[1]),
                                    Double.parseDouble(parts[2]),
                                    Double.parseDouble(parts[3]),
                                    Float.parseFloat(parts[4]),
                                    Float.parseFloat(parts[5])
                            );
                            player.teleport(homeLocation);
                            player.sendMessage(messages.getReplacedMessage("teleported-home"));
                        }
                    } else {
                        player.sendMessage(messages.getReplacedMessage("no-home"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
