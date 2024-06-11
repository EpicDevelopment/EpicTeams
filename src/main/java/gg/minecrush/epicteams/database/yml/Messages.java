package gg.minecrush.epicteams.database.yml;

import gg.minecrush.epicteams.util.color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import gg.minecrush.epicteams.database.yml.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Messages {

    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;
    private final Config configManager;
    private String filePath = "language.yml";

    public Messages(Plugin plugin, Config config) {
        this.plugin = plugin;
        this.configManager = config;
        createConfig();
    }

    public String getFilePath() {
        return filePath;
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), filePath);
        if (!configFile.exists()) {
            plugin.saveResource(filePath, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getMessages(String key) {
        String message = config.getString(key);
        if (message == null) {
            return "";
        }
        return color.c(message);
    }

    public String getReplacedMessage(String key) {
        String message = config.getString(key);
        if (message == null) {
            return "";
        }
        return color.c(message).replace("%p%", this.getMessages("prefix")).replace("%tp%", this.getMessages("team-prefix"));
    }

    public List<String> getArrayMessages(String key) {
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
            configFile = new File(plugin.getDataFolder(), filePath);
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
