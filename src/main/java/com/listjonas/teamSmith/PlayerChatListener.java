package com.listjonas.teamSmith;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

public class PlayerChatListener implements Listener {

    private final TeamManager teamManager;

    public PlayerChatListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Team team = teamManager.getPlayerTeam(player);

        if (team != null) {
            String prefixText = team.getPrefix();
            String prefixColor = team.getPrefixColor();
            if (prefixText != null && !prefixText.isEmpty()) {
                String coloredPrefix = ChatColor.translateAlternateColorCodes('&', prefixColor + prefixText);
                event.setFormat(coloredPrefix + ChatColor.RESET + event.getFormat()); // Added ChatColor.RESET to prevent color bleeding
            }
        }
    }
}