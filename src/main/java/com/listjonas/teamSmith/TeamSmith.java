package com.listjonas.teamSmith;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.listeners.EntityDamageListener;
import com.listjonas.teamSmith.listeners.PlayerChatListener;
import com.listjonas.teamSmith.listeners.PlayerJoinListener;
import com.listjonas.teamSmith.listeners.PlayerQuitListener;
import com.listjonas.teamSmith.data.ConfigData;
import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamSmith extends JavaPlugin {
    private TeamManager teamManager;
    private ConfigData configData;
    private static TeamSmith instance;

    @Override
    public void onEnable() {
        instance = this;
        configData = new ConfigData(this);
        teamManager = TeamManager.createInstance(this);

        // Register commands programmatically for Paper compatibility
        TeamCommand teamExecutor = new TeamCommand(this, teamManager);
        try {
            // Obtain CommandMap
            final java.lang.reflect.Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) bukkitCommandMap.get(getServer());

            // Create PluginCommand instance using reflection
            java.lang.reflect.Constructor<org.bukkit.command.PluginCommand> constructor = 
                org.bukkit.command.PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            org.bukkit.command.PluginCommand command = constructor.newInstance("team", this);

            // Set command properties
            command.setExecutor(teamExecutor);
            command.setTabCompleter(teamExecutor);
            command.setAliases(java.util.Arrays.asList("ts", "myteam"));
            command.setDescription("Main command for TeamSmith plugin.");
            command.setUsage("/team <subcommand> [args]");
            
            // Register the command
            commandMap.register(getDescription().getName().toLowerCase().replace(" ", "_"), command);
            getLogger().info("TeamSmith command 'team' registered programmatically.");

        } catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Error registering command 'team' programmatically", e);
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerChatListener(teamManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(teamManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(teamManager), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(teamManager), this);

        // Schedule a repeating task to update the tab list footer
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            if (teamManager != null) {
                teamManager.updateTabListFooterForAllPlayers();
            }
        }, 0L, configData.getRamUpdateFrequencyTicks());

        getLogger().info("TeamSmith plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (teamManager != null) {
            teamManager.saveTeams();
        }
        getLogger().info("TeamSmith plugin has been disabled!");
    }

    public static TeamSmith getInstance() {
        return instance;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public ConfigData getConfigData() {
        return configData;
    }
}
