package com.listjonas.teamSmith.data;

import com.listjonas.teamSmith.TeamSmith;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataManager {

    private final TeamSmith plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;
    private final String fileName;

    public DataManager(TeamSmith plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), fileName);
        }
        dataConfig = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        // InputStream defConfigStream = plugin.getResource(fileName);
        // if (defConfigStream != null) {
        // YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
        // dataConfig.setDefaults(defConfig);
        // }
    }

    public FileConfiguration getConfig() {
        if (dataConfig == null) {
            reloadConfig();
        }
        return dataConfig;
    }

    public void saveConfig() {
        if (dataConfig == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save config to " + configFile + ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), fileName);
        }
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    /**
     * Saves a map of data to a specific path in the YAML file.
     * Each key in the outer map will become a main section in the YAML.
     * @param path The base path to save the data under (e.g., "teams").
     * @param dataMap A map where keys are identifiers (e.g., team names) and values are maps of their properties.
     */
    public void saveData(String path, Map<String, Map<String, Object>> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            getConfig().set(path, null); // Clear the path if data is empty
        } else {
            for (Map.Entry<String, Map<String, Object>> entry : dataMap.entrySet()) {
                getConfig().set(path + "." + entry.getKey(), entry.getValue());
            }
        }
        saveConfig();
    }

    /**
     * Loads data from a specific path in the YAML file.
     * @param path The base path to load data from (e.g., "teams").
     * @return A map where keys are identifiers (e.g., team names) and values are maps of their properties.
     */
    public Map<String, Map<String, Object>> loadData(String path) {
        Map<String, Map<String, Object>> loadedData = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                ConfigurationSection subSection = section.getConfigurationSection(key);
                if (subSection != null) {
                    loadedData.put(key, new HashMap<>(subSection.getValues(false)));
                }
            }
        }
        return loadedData;
    }

     /**
     * Deletes a specific entry from a path in the YAML file.
     * @param path The base path (e.g., "teams").
     * @param key The key of the entry to delete (e.g., team name).
     */
    public void deleteDataEntry(String path, String key) {
        getConfig().set(path + "." + key, null);
        saveConfig();
    }

    /**
     * Saves a list of strings to a specific path in the YAML file.
     * @param path The base path to save the data under (e.g., "alliances.teamA").
     * @param dataList The list of strings to save.
     */
    public void saveStringList(String path, List<String> dataList) {
        getConfig().set(path, dataList);
        saveConfig();
    }

    /**
     * Loads a list of strings from a specific path in the YAML file.
     * @param path The base path to load data from (e.g., "alliances.teamA").
     * @return A list of strings, or an empty list if not found.
     */
    public List<String> loadStringList(String path) {
        return getConfig().getStringList(path);
    }
}