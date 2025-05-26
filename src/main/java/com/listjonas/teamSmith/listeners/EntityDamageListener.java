package com.listjonas.teamSmith.listeners;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.listjonas.teamSmith.commands.TeamCommand.INFO_COLOR;

public class EntityDamageListener implements Listener {

    private final TeamManager teamManager;

    public EntityDamageListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return; // Only interested in Player vs Player damage
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        Team victimTeam = teamManager.getPlayerTeam(victim);
        Team attackerTeam = teamManager.getPlayerTeam(attacker);

        // Check if both players are in the same team and friendly fire is disabled
        if (victimTeam != null && victimTeam.equals(attackerTeam)) {
            if (!victimTeam.isFriendlyFireEnabled()) {
                event.setCancelled(true); // Cancel damage if friendly fire is off
                // Send a message to the attacker
                attacker.sendMessage(INFO_COLOR + "Friendly fire is disabled in your team!");
            }
        }
    }
}