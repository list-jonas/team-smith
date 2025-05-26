package com.listjonas.teamSmith.listeners;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final TeamManager teamManager;

    public PlayerJoinListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Team playerTeam = teamManager.getPlayerTeam(player);

        if (playerTeam != null) {
            String motd = playerTeam.getTeamMotd();
            if (motd != null && !motd.isEmpty()) {
                // Delay sending the MOTD slightly to ensure it's visible after other join messages
                TeamSmith.getInstance().getServer().getScheduler().runTaskLater(TeamSmith.getInstance(), () -> {
                    player.sendMessage(ChatColor.GOLD + "[" + playerTeam.getName() + " MOTD] " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', motd));
                }, 20L); // 20 ticks = 1 second
            }
        }
    }
}