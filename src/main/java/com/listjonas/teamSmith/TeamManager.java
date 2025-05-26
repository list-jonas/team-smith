package com.listjonas.teamSmith;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.listjonas.teamsmith.Team;

public class TeamManager {
    private final TeamSmith plugin;
    private final Map<String, com.listjonas.teamsmith.Team> teams;
    private final Map<UUID, String> playerTeamMap; // To quickly find a player's team

    public TeamManager(TeamSmith plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeamMap = new HashMap<>();
        // Load teams from storage if implemented
    }

    public boolean createTeam(String teamName, Player leader) {
        if (teams.containsKey(teamName.toLowerCase())) {
            leader.sendMessage("A team with that name already exists.");
            return false;
        }
        if (getPlayerTeam(leader) != null) {
            leader.sendMessage("You are already in a team.");
            return false;
        }
        Team newTeam = new Team(teamName, leader);
        teams.put(teamName.toLowerCase(), newTeam);
        playerTeamMap.put(leader.getUniqueId(), teamName.toLowerCase());
        leader.sendMessage("Team '" + teamName + "' created successfully!");
        return true;
    }

    public boolean deleteTeam(String teamName, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage("Team '" + teamName + "' not found.");
            return false;
        }
        if (!team.isLeader(requester)) {
            requester.sendMessage("Only the team leader can delete the team.");
            return false;
        }
        for (UUID memberId : team.getMembers()) {
            playerTeamMap.remove(memberId);
        }
        teams.remove(teamName.toLowerCase());
        requester.sendMessage("Team '" + teamName + "' has been disbanded.");
        // Notify members if needed
        return true;
    }

    public Team getTeam(String teamName) {
        return teams.get(teamName.toLowerCase());
    }

    public Team getPlayerTeam(Player player) {
        String teamName = playerTeamMap.get(player.getUniqueId());
        if (teamName != null) {
            return getTeam(teamName);
        }
        return null;
    }

    public boolean addPlayerToTeam(String teamName, Player playerToAdd, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage("Team '" + teamName + "' not found.");
            return false;
        }
        // Potentially add an invitation system here
        if (getPlayerTeam(playerToAdd) != null) {
            requester.sendMessage(playerToAdd.getName() + " is already in a team.");
            playerToAdd.sendMessage("You are already in a team.");
            return false;
        }
        if (team.addMember(playerToAdd)) {
            playerTeamMap.put(playerToAdd.getUniqueId(), teamName.toLowerCase());
            requester.sendMessage(playerToAdd.getName() + " has been added to team '" + teamName + "'.");
            playerToAdd.sendMessage("You have joined team '" + teamName + "'.");
            return true;
        }
        return false;
    }

    public boolean removePlayerFromTeam(String teamName, Player playerToRemove, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage("Team '" + teamName + "' not found.");
            return false;
        }
        if (!team.isMember(playerToRemove)) {
            requester.sendMessage(playerToRemove.getName() + " is not in that team.");
            return false;
        }

        if (team.isLeader(playerToRemove)) {
            if (team.getSize() > 1) {
                requester.sendMessage("The leader cannot leave the team. Transfer leadership first or disband the team.");
                return false;
            } else {
                // Last member is leader, so disband team
                return deleteTeam(teamName, requester);
            }
        }

        if (team.removeMember(playerToRemove)) {
            playerTeamMap.remove(playerToRemove.getUniqueId());
            requester.sendMessage(playerToRemove.getName() + " has been removed from team '" + teamName + "'.");
            playerToRemove.sendMessage("You have been removed from team '" + teamName + "'.");
            return true;
        }
        return false;
    }

    public boolean transferLeadership(String teamName, Player newLeader, Player currentLeader) {
        Team team = getTeam(teamName);
        if (team == null) {
            currentLeader.sendMessage("Team '" + teamName + "' not found.");
            return false;
        }
        if (!team.isLeader(currentLeader)) {
            currentLeader.sendMessage("Only the current team leader can transfer leadership.");
            return false;
        }
        if (!team.isMember(newLeader)) {
            currentLeader.sendMessage(newLeader.getName() + " is not a member of your team.");
            return false;
        }
        team.setLeader(newLeader.getUniqueId());
        currentLeader.sendMessage("Leadership of team '" + teamName + "' transferred to " + newLeader.getName() + ".");
        newLeader.sendMessage("You are now the leader of team '" + teamName + "'.");
        return true;
    }

    public boolean setTeamPrefix(String teamName, String prefix, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage("Team '" + teamName + "' not found.");
            return false;
        }
        if (!team.isLeader(requester)) {
            requester.sendMessage("Only the team leader can set the team prefix.");
            return false;
        }
        team.setPrefix(prefix);
        requester.sendMessage("Team '" + teamName + "' prefix set to '" + prefix + "'.");
        return true;
    }
    
    // Consider adding methods for saving/loading teams to a file (e.g., YAML or JSON)
    // public void loadTeams() { ... }
    // public void saveTeams() { ... }
}