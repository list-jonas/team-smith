package com.listjonas.teamSmith.listeners;

import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final TeamManager teamManager;

    public PlayerQuitListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Update the tab list names and footer for all players when a player quits
        // It's good to run this slightly delayed to ensure the player is fully gone from lists
        // However, for tab list updates, immediate might be fine or even preferred.
        // For now, let's do it immediately.
        teamManager.updateAllPlayersTabNamesAndFooter();
    }
}