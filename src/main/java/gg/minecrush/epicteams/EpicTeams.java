package gg.minecrush.epicteams;

import gg.minecrush.epicteams.api.TeamsExpansion;
import gg.minecrush.epicteams.commands.teamadmin.TeamAdminCommand;
import gg.minecrush.epicteams.commands.teamadmin.TeamAdminTabComplete;
import gg.minecrush.epicteams.commands.teams.TeamCommand;
import gg.minecrush.epicteams.commands.teams.TeamTabComplete;
import gg.minecrush.epicteams.database.sqlite.SQLite;
import gg.minecrush.epicteams.database.yml.Config;
import gg.minecrush.epicteams.database.yml.Messages;
import gg.minecrush.epicteams.listeners.ChatListener;
import gg.minecrush.epicteams.listeners.CombatListener;
import gg.minecrush.epicteams.listeners.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;

import java.io.File;

public final class EpicTeams extends JavaPlugin {

    private SQLite sqLite;
    private Messages messagesConfig;
    private Config config;
    private static Chat chat = null;

    @Override
    public void onEnable() {


        try {
            File configFiles = new File(getDataFolder(), "config.yml");
            if (!configFiles.exists()) {
                saveResource("config.yml", false);
            }
        } catch (Exception e) {
            getLogger().severe("[EpicTeams] Failed to create configuration file");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        this.config = new Config(this);

        try {
            File msgFile = new File(getDataFolder(), "language.yml");
            if (!msgFile.exists()) {
                saveResource("language.yml", false);
            }
        } catch (Exception e) {
            getLogger().severe("[EpicTeams] Failed to create language file");
            Bukkit.getPluginManager().disablePlugin(this);
        }



        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            sqLite = new SQLite(getDataFolder().getAbsolutePath() + "/database.db");
            if (sqLite != null) {
                getLogger().info("SQLite initialized successfully.");
            } else {
                getLogger().severe("SQLite initialization failed.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } catch (Exception e) {
            getLogger().severe("SQLite initialization failed.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TeamsExpansion(this, sqLite).register();
        } else {
            getLogger().severe("-------------------------------------");
            getLogger().severe("While looking for PlaceholderAPI");
            getLogger().severe("");
            getLogger().severe("Could not find PlaceholderAPI! This plugin is required.");
            getLogger().severe("-------------------------------------");
        }



        if (sqLite != null) {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                if (!setupChat()){
                    getLogger().severe("Chat initialization with Vault failed.");
                }
            } else {
                getLogger().severe("-------------------------------------");
                getLogger().severe(" ");
                getLogger().severe("While looking for vault:");
                getLogger().severe(" ");
                getLogger().severe("Vault is not enabled on this server.");
                getLogger().severe(" ");
                getLogger().severe("-------------------------------------");
            }
            this.messagesConfig = new Messages(this, config);
            this.config = new Config(this);

            this.getServer().getPluginManager().registerEvents(new CombatListener(sqLite), this);
            this.getServer().getPluginManager().registerEvents(new JoinListener(sqLite), this);
            this.getServer().getPluginManager().registerEvents(new ChatListener(sqLite, messagesConfig, this), this);
            this.getCommand("team").setExecutor(new TeamCommand(sqLite, messagesConfig, config, this, this));
            this.getCommand("team").setTabCompleter(new TeamTabComplete(sqLite, config));
            this.getCommand("teamadmin").setExecutor(new TeamAdminCommand(sqLite, messagesConfig, config, this));
            this.getCommand("teamadmin").setTabCompleter(new TeamAdminTabComplete());
        }
    }



    public String getPlayerPrefix(Player player) {
        if (config.getValueBoolean("vault-prefix")){
            if (chat != null) {
                return chat.getPlayerPrefix(player);
            }
            return "";
        } else {
            return "null-prefix";
        }
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    @Override
    public void onDisable() {
        try {
            sqLite.clearInvitations();
            sqLite.closeConnect();
        } catch (Exception e) {
            getLogger().severe("[EpicTeams] Failed to close database");
        }
    }
}
