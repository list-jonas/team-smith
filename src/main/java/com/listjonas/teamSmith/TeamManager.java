package com.listjonas.teamSmith;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private final TeamSmith plugin;
    private final Map<String, Team> teams;
    private final Map<UUID, String> playerTeamMap; // To quickly find a player's team
    private final DataManager dataManager;
    private static final String TEAMS_DATA_FILE = "teams.yml";
    private static final String TEAMS_CONFIG_PATH = "teams";

    public TeamManager(TeamSmith plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeamMap = new HashMap<>();
        this.dataManager = new DataManager(plugin, TEAMS_DATA_FILE);
        loadTeams();
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
        saveTeams(); // Save after creating
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
        dataManager.deleteDataEntry(TEAMS_CONFIG_PATH, teamName.toLowerCase()); // Remove from data file
        // saveTeams(); // Alternative: resave all, but deleteDataEntry is more direct for removal
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
            saveTeams(); // Save after adding member
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
            saveTeams(); // Save after removing member
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
        saveTeams(); // Save after transferring leadership
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
        saveTeams(); // Save after setting prefix
        requester.sendMessage("Team '" + teamName + "' prefix set to '" + prefix + "'.");
        return true;
    }

    public void loadTeams() {
        Map<String, Map<String, Object>> teamsData = dataManager.loadData(TEAMS_CONFIG_PATH);
        if (teamsData.isEmpty()) {
            plugin.getLogger().info("No teams found in " + TEAMS_DATA_FILE + ", or file is empty.");
            return;
        }
        for (Map.Entry<String, Map<String, Object>> entry : teamsData.entrySet()) {
            String teamName = entry.getKey();
            Team team = new Team(teamName, entry.getValue());
            teams.put(teamName.toLowerCase(), team);
            for (UUID memberId : team.getMembers()) {
                playerTeamMap.put(memberId, teamName.toLowerCase());
            }
        }
        plugin.getLogger().info("Loaded " + teams.size() + " teams from " + TEAMS_DATA_FILE);
    }

    public void saveTeams() {
        Map<String, Map<String, Object>> teamsToSave = new HashMap<>();
        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            teamsToSave.put(entry.getKey(), entry.getValue().serialize());
        }
        dataManager.saveData(TEAMS_CONFIG_PATH, teamsToSave);
        plugin.getLogger().info("Saved " + teamsToSave.size() + " teams to " + TEAMS_DATA_FILE);
    }
}