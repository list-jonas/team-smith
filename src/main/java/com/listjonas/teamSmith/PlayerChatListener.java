package com.listjonas.teamSmith;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.listjonas.teamsmith.Team;

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
            String prefix = team.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                event.setFormat(prefix + event.getFormat());
            }
        }
    }
}