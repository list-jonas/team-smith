package com.listjonas.teamSmith;

import org.bukkit.plugin.java.JavaPlugin;

public class TeamSmith extends JavaPlugin {

    private static TeamSmith instance;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        instance = this;
        teamManager = new TeamManager(this);

        // Register commands
        TeamCommand teamCommand = new TeamCommand(this, teamManager);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerChatListener(teamManager), this);

        getLogger().info("TeamSmith plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Potentially save teams here if implementing persistence
        // teamManager.saveTeams();
        getLogger().info("TeamSmith plugin has been disabled!");
    }

    public static TeamSmith getInstance() {
        return instance;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}
