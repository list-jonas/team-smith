package com.listjonas.teamSmith.listeners;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import static com.listjonas.teamSmith.commands.TeamCommand.INFO_COLOR;
import static com.listjonas.teamSmith.commands.TeamCommand.MSG_PREFIX;

public class EntityDamageListener implements Listener {

    private final TeamManager teamManager;

    public EntityDamageListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                attacker = (Player) shooter;
            }
        }

        if (attacker == null) {
            return; // Attacker is not a player or a player's projectile
        }

        Player victimPlayer = null;
        if (event.getEntity() instanceof Player) {
            victimPlayer = (Player) event.getEntity();
        } else if (event.getEntity() instanceof Tameable) {
            Tameable tameable = (Tameable) event.getEntity();
            if (tameable.isTamed() && tameable.getOwner() instanceof Player) {
                victimPlayer = (Player) tameable.getOwner();
            }
        }

        if (victimPlayer == null) {
            return; // Victim is not a player or a tamed animal owned by a player
        }

        Team attackerTeam = teamManager.getPlayerTeam(attacker);
        Team victimTeam = teamManager.getPlayerTeam(victimPlayer);

        // Check if both players (or player and animal's owner) are in the same team and friendly fire is disabled
        if (attackerTeam != null && attackerTeam.equals(victimTeam)) {
            if (!attackerTeam.isFriendlyFireEnabled()) {
                event.setCancelled(true); // Cancel damage if friendly fire is off
                // Send a message to the attacker
                attacker.sendMessage(MSG_PREFIX + INFO_COLOR + "Friendly fire is disabled in your team!");
            }
        }
    }
}