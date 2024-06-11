package gg.minecrush.epicteams.database.sqlite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLite {
    private Connection connection;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type listType = new TypeToken<List<UUID>>() {}.getType();

    public SQLite(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    team TEXT DEFAULT '',
                    teamchat BOOLEAN DEFAULT FALSE
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    name TEXT PRIMARY KEY,
                    display_name TEXT NOT NULL,
                    kills INTEGER DEFAULT 0,
                    deaths INTEGER DEFAULT 0,
                    members TEXT DEFAULT '[]',
                    moderators TEXT DEFAULT '[]',
                    home TEXT DEFAULT '',
                    owner TEXT NOT NULL,
                    level INTEGER DEFAULT 1
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS invitations (
                    uuid TEXT,
                    team_name TEXT,
                    PRIMARY KEY (uuid, team_name)
                )
            """);
        }
    }

    public void clearInvitations() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM invitations");
        }
    }

    public void addInvite(UUID uuid, String teamName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO invitations (uuid, team_name) VALUES(?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, teamName);
            ps.executeUpdate();
        }
    }



    public boolean hasInvite(UUID uuid, String teamName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM invitations WHERE uuid = ? AND team_name = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, teamName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<String> getInvites(UUID uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT team_name FROM invitations WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<String> invites = new ArrayList<>();
                while (rs.next()) {
                    invites.add(rs.getString("team_name"));
                }
                return invites;
            }
        }
    }

    public void teamAnnounce(String team, String announcement) throws SQLException {
        List<UUID> mods = getTeamModerators(team);
        List<UUID> members = getTeamMembers(team);
        for (UUID mod : mods) {
            if (mod == null) {
                continue;
            }
            Player modUser = Bukkit.getPlayer(mod);
            if (modUser != null) {
                modUser.sendMessage(announcement);
            }
        }
        for (UUID member : members) {
            if (member == null) {
                continue;
            }
            Player memberUser = Bukkit.getPlayer(member);
            if (memberUser != null) {
                memberUser.sendMessage(announcement);
            }
        }
        UUID ownerUUID = safeUUIDFromString(getTeamOwner(team));
        if (ownerUUID != null) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null) {
                owner.sendMessage(announcement);
            }
        }
    }

    private UUID safeUUIDFromString(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeInvite(UUID uuid, String teamName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM invitations WHERE uuid = ? AND team_name = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, teamName);
            ps.executeUpdate();
        }
    }

    public void closeConnect() throws SQLException {
        if (connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    public void registerPlayer(OfflinePlayer p) throws SQLException {
        if (!playerExists(p.getName())) {
            try (PreparedStatement ps = connection.prepareStatement(("INSERT INTO players (uuid, username) VALUES(?, ?)"))){
                ps.setString(1, p.getUniqueId().toString());
                ps.setString(2, p.getName());
                ps.executeUpdate();
            }
        }
    }

    public boolean playerExists(String uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")){
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return true;
                }
                return false;
            }
        }
    }

    public void updatePlayerteam(OfflinePlayer p, String team) throws SQLException {
        if (!playerExists(p.getUniqueId().toString())){
            registerPlayer(p);
        } else {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET team = ? WHERE uuid = ?")){
                ps.setString(1, team);
                ps.setString(2, p.getUniqueId().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updatePlayername(Player p, String username) throws SQLException {
        if (!playerExists(p.getUniqueId().toString())){
            registerPlayer(p);
        } else {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET username = ? WHERE uuid = ?")){
                ps.setString(1, username);
                ps.setString(2, p.getUniqueId().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updatePlayerteamchat(OfflinePlayer p, boolean teamchat) throws SQLException {
        if (!playerExists(p.getUniqueId().toString())){
            registerPlayer(p);
        } else {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET teamchat = ? WHERE uuid = ?")){
                ps.setBoolean(1, teamchat);
                ps.setString(2, p.getUniqueId().toString());
                ps.executeUpdate();
            }
        }
    }

    public String getTeam(OfflinePlayer p) throws SQLException {
        if (playerExists(p.getUniqueId().toString())) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT team FROM players WHERE uuid = ?")) {
                ps.setString(1, p.getUniqueId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("team");
                    }
                }
            }
        }
        return "";
    }



    public String getName(Player p) throws SQLException {
        if (playerExists(p.getUniqueId().toString())) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT username FROM players WHERE uuid = ?")) {
                ps.setString(1, p.getUniqueId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        }
        return "";
    }

    public String getNames(UUID p) throws SQLException {
        if (playerExists(p.toString())) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT username FROM players WHERE uuid = ?")) {
                ps.setString(1, p.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        }
        return "";
    }
    public Boolean getTeamchat(Player p) throws SQLException {
        if (playerExists(p.getUniqueId().toString())){
            try (PreparedStatement ps = connection.prepareStatement("SELECT teamchat FROM players WHERE uuid = ?")){
                ps.setString(1, p.getUniqueId().toString());
                try (ResultSet rs = ps.executeQuery()){
                    if (rs.next()){
                        return rs.getBoolean("teamchat");
                    }
                }
            }
        }
        return null;
    }

    public void registerTeam(String name, String displayName, String owner) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO teams (name, display_name, owner) VALUES(?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, displayName);
            ps.setString(3, owner);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET team = ? WHERE uuid = ?")) {
            ps.setString(1, name);
            ps.setString(2, owner);
            ps.executeUpdate();
        }
    }

    public void deleteTeam(String teamName) throws SQLException {
        String owner = getTeamOwner(teamName);

        try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET team = ? WHERE uuid = ?")) {
            ps.setString(1, "");
            ps.setString(2, owner);
            ps.executeUpdate();
        }

        List<UUID> members = getTeamMembers(teamName);
        for (UUID memberUUID : members) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET team = ? WHERE uuid = ?")) {
                ps.setString(1, "");
                ps.setString(2, memberUUID.toString());
                ps.executeUpdate();
            }
        }

        List<UUID> mods = getTeamModerators(teamName);
        for (UUID modsUUID : members) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET team = ? WHERE uuid = ?")) {
                ps.setString(1, "");
                ps.setString(2, modsUUID.toString());
                ps.executeUpdate();
            }
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM teams WHERE name = ?")) {
            ps.setString(1, teamName);
            ps.executeUpdate();
        }
    }


    public boolean teamExists(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateTeamKills(String name, int kills) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET kills = ? WHERE name = ?")) {
            ps.setInt(1, kills);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void updateTeamDeaths(String name, int deaths) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET deaths = ? WHERE name = ?")) {
            ps.setInt(1, deaths);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void updateTeamHome(String name, String home) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET home = ? WHERE name = ?")) {
            ps.setString(1, home);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void updateTeamMembers(String name, List<UUID> members) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET members = ? WHERE name = ?")) {
            ps.setString(1, gson.toJson(members, listType));
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void addTeamMember(String teamName, UUID memberUUID) throws SQLException {
        List<UUID> members = getTeamMembers(teamName);
        if (!members.contains(memberUUID)) {
            members.add(memberUUID);
            updateTeamMembers(teamName, members);
        }
    }

    public void removeTeamMember(String teamName, UUID memberUUID) throws SQLException {
        List<UUID> members = getTeamMembers(teamName);
        if (members.contains(memberUUID)) {
            members.remove(memberUUID);
            updateTeamMembers(teamName, members);
        }
    }

    public void updateTeamModerators(String name, List<UUID> moderators) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET moderators = ? WHERE name = ?")) {
            ps.setString(1, gson.toJson(moderators, listType));
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void addTeamModerator(String teamName, UUID moderatorUUID) throws SQLException {
        List<UUID> moderators = getTeamModerators(teamName);
        if (!moderators.contains(moderatorUUID)) {
            moderators.add(moderatorUUID);
            updateTeamModerators(teamName, moderators);
        }
    }

    public void removeTeamModerator(String teamName, UUID moderatorUUID) throws SQLException {
        List<UUID> moderators = getTeamModerators(teamName);
        if (moderators.contains(moderatorUUID)) {
            moderators.remove(moderatorUUID);
            updateTeamModerators(teamName, moderators);
        }
    }

    public void updateTeamOwner(String name, String owner) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET owner = ? WHERE name = ?")) {
            ps.setString(1, owner);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public void updateTeamLevel(String name, int level) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE teams SET level = ? WHERE name = ?")) {
            ps.setInt(1, level);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public String getTeamDisplayName(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT display_name FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("display_name");
                }
            }
        }
        return null;
    }

    public int getTeamKills(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT kills FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("kills");
                }
            }
        }
        return 0;
    }

    public String getTeamHome(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT home FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("home");
                }
            }
        }
        return "";
    }

    public int getTeamDeaths(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT deaths FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("deaths");
                }
            }
        }
        return 0;
    }

    public List<UUID> getTeamMembers(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT members FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return gson.fromJson(rs.getString("members"), listType);
                }
            }
        }
        return new ArrayList<>();
    }

    public List<UUID> getTeamModerators(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT moderators FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return gson.fromJson(rs.getString("moderators"), listType);
                }
            }
        }
        return new ArrayList<>();
    }

    public String getTeamOwner(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT owner FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("owner");
                }
            }
        }
        return null;
    }

    public int getTeamLevel(String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT level FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level");
                }
            }
        }
        return 1;
    }
}
