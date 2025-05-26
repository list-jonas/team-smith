package com.listjonas.teamSmith;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;
import java.util.logging.Level;

public class TeamSmith extends JavaPlugin {

    private static TeamSmith instance;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        instance = this;
        teamManager = new TeamManager(this);

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
}
