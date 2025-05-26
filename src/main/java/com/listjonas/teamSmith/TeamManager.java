package com.listjonas.teamSmith;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

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

    // Define some standard colors for messages
    private static final String MSG_PREFIX = ChatColor.GOLD + "[TeamSmith] " + ChatColor.RESET;
    private static final String SUCCESS_COLOR = ChatColor.GREEN.toString();
    private static final String ERROR_COLOR = ChatColor.RED.toString();
    private static final String INFO_COLOR = ChatColor.YELLOW.toString();
    private static final String ACCENT_COLOR = ChatColor.AQUA.toString();

    public TeamManager(TeamSmith plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeamMap = new HashMap<>();
        this.dataManager = new DataManager(plugin, TEAMS_DATA_FILE);
        loadTeams();
    }

    public boolean createTeam(String teamName, Player leader) {
        if (teams.containsKey(teamName.toLowerCase())) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "A team with the name " + ACCENT_COLOR + teamName + ERROR_COLOR + " already exists.");
            return false;
        }
        if (getPlayerTeam(leader) != null) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are already in a team.");
            return false;
        }
        Team newTeam = new Team(teamName, leader);
        teams.put(teamName.toLowerCase(), newTeam);
        playerTeamMap.put(leader.getUniqueId(), teamName.toLowerCase());
        saveTeams(); // Save after creating
        leader.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " created successfully!");
        return true;
    }

    public boolean deleteTeam(String teamName, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (!team.isLeader(requester)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team leader can delete the team.");
            return false;
        }
        for (UUID memberId : team.getMembers()) {
            playerTeamMap.remove(memberId);
        }
        teams.remove(teamName.toLowerCase());
        dataManager.deleteDataEntry(TEAMS_CONFIG_PATH, teamName.toLowerCase()); // Remove from data file
        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " has been disbanded.");
        // Notify members if needed (e.g., loop through team.getMembers() before clearing)
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
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        // Potentially add an invitation system here
        if (getPlayerTeam(playerToAdd) != null) {
            requester.sendMessage(MSG_PREFIX + INFO_COLOR + playerToAdd.getName() + " is already in a team.");
            playerToAdd.sendMessage(MSG_PREFIX + INFO_COLOR + "You are already in a team.");
            return false;
        }
        if (team.addMember(playerToAdd)) {
            playerTeamMap.put(playerToAdd.getUniqueId(), teamName.toLowerCase());
            saveTeams(); // Save after adding member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + playerToAdd.getName() + " has been added to team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            playerToAdd.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You have joined team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            return true;
        }
        return false;
    }

    public boolean removePlayerFromTeam(String teamName, Player playerToRemove, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (!team.isMember(playerToRemove)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + playerToRemove.getName() + " is not in that team.");
            return false;
        }

        if (team.isLeader(playerToRemove)) {
            if (team.getSize() > 1) {
                requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "The leader cannot leave the team. Transfer leadership first or disband the team.");
                return false;
            } else {
                // Last member is leader, so disband team
                return deleteTeam(teamName, requester); // deleteTeam already sends messages
            }
        }

        if (team.removeMember(playerToRemove)) {
            playerTeamMap.remove(playerToRemove.getUniqueId());
            saveTeams(); // Save after removing member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + playerToRemove.getName() + " has been removed from team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            playerToRemove.sendMessage(MSG_PREFIX + INFO_COLOR + "You have been removed from team " + ACCENT_COLOR + teamName + INFO_COLOR + ".");
            return true;
        }
        return false;
    }

    public boolean transferLeadership(String teamName, Player newLeader, Player currentLeader) {
        Team team = getTeam(teamName);
        if (team == null) {
            currentLeader.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (!team.isLeader(currentLeader)) {
            currentLeader.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the current team leader can transfer leadership.");
            return false;
        }
        if (!team.isMember(newLeader)) {
            currentLeader.sendMessage(MSG_PREFIX + ERROR_COLOR + newLeader.getName() + " is not a member of your team.");
            return false;
        }
        if (newLeader.equals(currentLeader)){
            currentLeader.sendMessage(MSG_PREFIX + INFO_COLOR + "You are already the leader of this team.");
            return false;
        }
        team.setLeader(newLeader.getUniqueId());
        saveTeams(); // Save after transferring leadership
        currentLeader.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Leadership of team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " transferred to " + ACCENT_COLOR + newLeader.getName() + SUCCESS_COLOR + ".");
        newLeader.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You are now the leader of team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
        return true;
    }

    public boolean setTeamPrefix(String teamName, String prefix, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (!team.isLeader(requester)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team leader can set the team prefix.");
            return false;
        }
        team.setPrefix(prefix);
        saveTeams(); // Save after setting prefix
        String displayPrefix = ChatColor.translateAlternateColorCodes('&', team.getPrefixColor() + prefix);
        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " prefix set to: " + displayPrefix + ChatColor.RESET);
        return true;
    }

    public boolean setTeamPrefixColor(String teamName, String colorCode, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (!team.isLeader(requester)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team leader can set the team prefix color.");
            return false;
        }
        // Basic validation for color code (starts with '&' and is 2 chars long, e.g., "&c")
        if (!colorCode.matches("&[0-9a-fk-or]")) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Invalid color code. Use format like '&c' for red, '&1' for dark blue, etc.");
            requester.sendMessage(MSG_PREFIX + INFO_COLOR + "Valid colors: &0-&9, &a-&f, &k-&o, &r (reset).");
            return false;
        }
        team.setPrefixColor(colorCode);
        saveTeams(); // Save after setting prefix color
        String displayPrefix = ChatColor.translateAlternateColorCodes('&', team.getPrefixColor() + team.getPrefix());
        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " prefix color updated. Preview: " + displayPrefix + ChatColor.RESET);
        return true;
    }

    public void loadTeams() {
        Map<String, Map<String, Object>> teamsData = dataManager.loadData(TEAMS_CONFIG_PATH);
        if (teamsData == null || teamsData.isEmpty()) { // Added null check for teamsData
            plugin.getLogger().info("No teams found in " + TEAMS_DATA_FILE + ", or file is empty/corrupted.");
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