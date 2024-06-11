package gg.minecrush.epicteams.database.yml;

import gg.minecrush.epicteams.util.color;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getValue(String key) {
        String message = config.getString(key);
        if (message == null) {
            return "";
        }
        return color.c(message);
    }

    public int getValueInt(String key) {
        try {
            int message = Integer.parseInt(config.getString(key));
            return message;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getValueBoolean(String key) {
        try {
            boolean message = Boolean.parseBoolean(config.getString(key));
            return message;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<String> getBannedNames() {
        return config.getStringList("banned-names");
    }

    public List<String> getArrayValue(String key) {
        List<String> messages = config.getStringList(key);
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> coloredMessages = new ArrayList<>();
        for (String message : messages) {
            coloredMessages.add(color.c(message));
        }
        return coloredMessages;
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
