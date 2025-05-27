package com.listjonas.teamSmith.listeners;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.Objects;
import java.util.UUID;

public class ScoreboardListener implements Listener {

    private final TeamManager teamManager;
    private final TeamSmith plugin;

    public ScoreboardListener(TeamManager teamManager, TeamSmith plugin) {
        this.teamManager = teamManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay scoreboard update slightly to ensure all plugins have loaded player data
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateScoreboard(player), 20L); 
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null && player.getScoreboard() != null && player.getScoreboard().getObjective(plugin.getName()) != null) {
            player.setScoreboard(manager.getNewScoreboard()); // Clear scoreboard on quit
        }
    }

    public void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = player.getScoreboard();
        // Use existing board if it's from this plugin, otherwise create new
        if (board == null || board.getObjective(DisplaySlot.SIDEBAR) == null || !board.getObjective(DisplaySlot.SIDEBAR).getName().equals("team_scoreboard_obj")) {
            board = manager.getNewScoreboard();
        }

        Objective objective = board.getObjective("team_scoreboard_obj");
        if (objective == null) {
            objective = board.registerNewObjective("team_scoreboard_obj", "dummy", ChatColor.GOLD.toString() + ChatColor.BOLD + "Team Info");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            // Clear old scores before adding new ones to prevent clutter
            for (String entry : board.getEntries()) {
                board.resetScores(entry);
            }
            objective.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "Team Info"); // Ensure display name is correct
        }

        Team playerTeam = teamManager.getPlayerTeam(player);
        int currentScore = 15;

        if (playerTeam != null) {
            String teamDisplayName = ChatColor.translateAlternateColorCodes('&', playerTeam.getPrefixColor() + playerTeam.getPrefix() + playerTeam.getName());
            objective.getScore(ChatColor.AQUA + "Team: " + teamDisplayName).setScore(currentScore--);

            UUID ownerId = playerTeam.getOwner();
            Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
            String ownerName = owner != null ? owner.getName() : (ownerId != null ? "Offline" : "N/A");
            objective.getScore(ChatColor.GREEN + "Owner: " + ChatColor.WHITE + ownerName).setScore(currentScore--);

            long onlineMembers = playerTeam.getMemberRoles().keySet().stream()
                                    .map(Bukkit::getPlayer)
                                    .filter(Objects::nonNull)
                                    .filter(Player::isOnline)
                                    .count();
            objective.getScore(ChatColor.GREEN + "Online: " + ChatColor.WHITE + onlineMembers + "/" + playerTeam.getSize()).setScore(currentScore--);
            
            String ffStatus = playerTeam.isFriendlyFireEnabled() ? ChatColor.RED + "ON" : ChatColor.GREEN + "OFF";
            objective.getScore(ChatColor.GREEN + "Friendly Fire: " + ffStatus).setScore(currentScore--);

            // Spacer
            if (onlineMembers > 0) {
                 objective.getScore(" ").setScore(currentScore--); 
                 objective.getScore(ChatColor.YELLOW + "Members:").setScore(currentScore--);
            }
           
            int membersListed = 0;
            for (UUID memberId : playerTeam.getMemberRoles().keySet()) {
                if (membersListed >= 5) break; // Limit displayed members to prevent overflow
                Player teamMember = Bukkit.getPlayer(memberId);
                if (teamMember != null && teamMember.isOnline()) {
                    String memberPrefix = ChatColor.translateAlternateColorCodes('&', playerTeam.getPrefixColor() + playerTeam.getPrefix());
                    objective.getScore(memberPrefix + teamMember.getName()).setScore(currentScore--);
                    membersListed++;
                }
            }

        } else {
            objective.getScore(ChatColor.GRAY + "You are not in a team.").setScore(currentScore--);
        }

        if (player.isOnline()) { // Ensure player is still online before setting scoreboard
             player.setScoreboard(board);
        }
    }

    // Call this method when a player joins/leaves a team or team details change
    public void updateScoreboardForAllTeamMembers(Team team) {
        if (team == null) return;
        for (UUID memberId : team.getMemberRoles().keySet()) {
            Player teamMember = Bukkit.getPlayer(memberId);
            if (teamMember != null && teamMember.isOnline()) {
                updateScoreboard(teamMember);
            }
        }
    }
    
    // Call this method when a player is no longer in any team
    public void clearScoreboardForPlayer(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getNewScoreboard());
        }
    }
}