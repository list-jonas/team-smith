package com.listjonas.teamSmith.data;

import com.listjonas.teamSmith.TeamSmith;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigData {

    private final TeamSmith plugin;
    private int maxWarps;
    private int ramUpdateFrequencyTicks;
    private int warpTimeoutSeconds;
    private int homeTimeoutSeconds;
    private int teleportDelaySeconds;

    public ConfigData(TeamSmith plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.maxWarps = config.getInt("max_warps", 3);
        this.ramUpdateFrequencyTicks = config.getInt("ram_update_frequency_seconds", 10) * 20; // Convert seconds to ticks
        this.warpTimeoutSeconds = config.getInt("warp_timeout_seconds", 180);
        this.homeTimeoutSeconds = config.getInt("home_timeout_seconds", 120);
        this.teleportDelaySeconds = config.getInt("teleport_delay_seconds", 3);
    }

    public int getMaxWarps() {
        return maxWarps;
    }

    public int getRamUpdateFrequencyTicks() {
        return ramUpdateFrequencyTicks;
    }

    public int getWarpTimeoutSeconds() {
        return warpTimeoutSeconds;
    }

    public int getHomeTimeoutSeconds() {
        return homeTimeoutSeconds;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }
}