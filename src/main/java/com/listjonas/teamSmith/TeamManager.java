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
        if (team.getPlayerRole(requester.getUniqueId()) != Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER can delete the team.");
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

        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        Team.Role targetRole = team.getPlayerRole(playerToRemove.getUniqueId());

        if (targetRole == Team.Role.OWNER) {
            if (team.getSize() > 1) {
                requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "The OWNER cannot be removed. Transfer ownership first or disband the team.");
                return false;
            }
            // If owner is the only member, let deleteTeam handle it (which checks for owner permission)
            return deleteTeam(teamName, requester); 
        }

        // Check if requester has permission to kick (Owner can kick anyone but self, Manager can kick Members)
        boolean canKick = false;
        if (requesterRole == Team.Role.OWNER && !requester.getUniqueId().equals(playerToRemove.getUniqueId())) {
            canKick = true;
        }
        if (requesterRole == Team.Role.MANAGER && targetRole == Team.Role.MEMBER) {
            canKick = true;
        }

        if (!canKick && !requester.getUniqueId().equals(playerToRemove.getUniqueId())) { // Player trying to leave themselves is always allowed if not owner
             requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "You do not have permission to remove this player.");
             return false;
        }
        
        // If player is trying to leave themselves (and is not the owner, handled above)
        if (requester.getUniqueId().equals(playerToRemove.getUniqueId()) && targetRole != Team.Role.OWNER) {
            // Allow self-leave
            team.removeMember(playerToRemove);
            playerTeamMap.remove(playerToRemove.getUniqueId());
            saveTeams(); // Save after removing member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You have been removed from team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            return true;
        } else if (!canKick) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "You do not have permission to remove this player.");
            return false;
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

    public boolean transferLeadership(String teamName, Player newLeaderPlayer, Player currentLeaderPlayer) {
        Team team = getTeam(teamName);
        if (team == null) {
            currentLeaderPlayer.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        if (team.getPlayerRole(currentLeaderPlayer.getUniqueId()) != Team.Role.OWNER) {
            currentLeaderPlayer.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the current team OWNER can transfer ownership.");
            return false;
        }
        if (!team.isMember(newLeaderPlayer)) {
            currentLeaderPlayer.sendMessage(MSG_PREFIX + ERROR_COLOR + newLeaderPlayer.getName() + " is not a member of your team.");
            return false;
        }
        if (newLeaderPlayer.equals(currentLeaderPlayer)){
            currentLeaderPlayer.sendMessage(MSG_PREFIX + INFO_COLOR + "You are already the OWNER of this team.");
            return false;
        }
        team.setOwner(newLeaderPlayer.getUniqueId()); // This method in Team.java now handles demoting old owner and promoting new one.
        saveTeams(); // Save after transferring leadership
        currentLeaderPlayer.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Ownership of team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " transferred to " + ACCENT_COLOR + newLeaderPlayer.getName() + SUCCESS_COLOR + ".");
        newLeaderPlayer.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You are now the OWNER of team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
        return true;
    }

    public boolean setTeamPrefix(String teamName, String prefix, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        if (requesterRole != Team.Role.OWNER && requesterRole != Team.Role.MANAGER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER or MANAGER can set the team prefix.");
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
        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        if (requesterRole != Team.Role.OWNER && requesterRole != Team.Role.MANAGER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER or MANAGER can set the team prefix color.");
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

    public boolean promotePlayerRole(String teamName, Player targetPlayer, Team.Role newRole, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }

        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        if (requesterRole != Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER can promote or demote members.");
            return false;
        }

        if (!team.isMember(targetPlayer)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + targetPlayer.getName() + " is not a member of this team.");
            return false;
        }

        Team.Role currentTargetRole = team.getPlayerRole(targetPlayer.getUniqueId());
        if (currentTargetRole == newRole) {
            requester.sendMessage(MSG_PREFIX + INFO_COLOR + targetPlayer.getName() + " already has the role " + ACCENT_COLOR + newRole.name() + INFO_COLOR + ".");
            return false;
        }

        if (newRole == Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Cannot promote to OWNER. Use /team transfer <player> instead.");
            return false;
        }
        
        if (targetPlayer.getUniqueId().equals(requester.getUniqueId()) && newRole != Team.Role.OWNER) {
             requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "You cannot change your own role with this command.");
             return false;
        }
        
        // Prevent demoting self if owner
        if (targetPlayer.getUniqueId().equals(requester.getUniqueId()) && currentTargetRole == Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "The OWNER cannot demote themselves. Transfer ownership first.");
            return false;
        }

        team.setPlayerRole(targetPlayer.getUniqueId(), newRole);
        saveTeams();

        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + targetPlayer.getName() + "'s role has been changed to " + ACCENT_COLOR + newRole.name() + SUCCESS_COLOR + ".");
        targetPlayer.sendMessage(MSG_PREFIX + INFO_COLOR + "Your role in team " + ACCENT_COLOR + team.getName() + INFO_COLOR + " has been changed to " + ACCENT_COLOR + newRole.name() + INFO_COLOR + ".");
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