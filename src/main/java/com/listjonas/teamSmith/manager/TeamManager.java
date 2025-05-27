package com.listjonas.teamSmith.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.data.DataManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.listjonas.teamSmith.commands.TeamCommand.*;

public class TeamManager {
    private final TeamSmith plugin;
    private final Map<String, Team> teams;
    private final Map<UUID, String> playerTeamMap; // To quickly find a player's team
    private final DataManager dataManager;
    private static final String TEAMS_DATA_FILE = "teams.yml";
    private static final String TEAMS_CONFIG_PATH = "teams";
    private static TeamManager instance;
    private final Multimap<UUID,String> pendingInvites = ArrayListMultimap.create();

    private TeamManager(TeamSmith plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeamMap = new HashMap<>();
        this.dataManager = new DataManager(plugin, TEAMS_DATA_FILE);
        loadTeams();
    }
    /**
     * Singleton instance of TeamManager.
     * Ensures only one instance exists throughout the plugin lifecycle.
     */
    public static TeamManager createInstance(TeamSmith plugin) {
        if (instance == null) {
            instance = new TeamManager(plugin);
        }
        return instance;
    }

    public static TeamManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TeamManager has not been initialized. Call createInstance() first.");
        }
        return instance;
    }

    public void updatePlayerTabName(Player player) {
        Team team = getPlayerTeam(player);
        if (team != null && team.getPrefix() != null && team.getPrefixColor() != null) {
            String prefix = ChatColor.translateAlternateColorCodes('&', team.getPrefixColor() + team.getPrefix());
            String newListName = prefix + " " + ChatColor.translateAlternateColorCodes('&', team.getPrefixColor() + player.getName());
            player.setPlayerListName(newListName);
        } else {
            player.setPlayerListName(player.getName()); // Reset if not in a team or prefix/color is null
        }
    }

    public void updateAllPlayersTabNamesAndFooter() {
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            updatePlayerTabName(onlinePlayer);
        }
        updateTabListFooterForAllPlayers();
        updateTabListHeaderForAllPlayers();
    }

    public void updateTabListFooterForAllPlayers() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = allocatedMemory - freeMemory;

        String footerText = String.format("%sRAM: %dMB / %dMB",
                                      ChatColor.GRAY, usedMemory, maxMemory) + "\n" + "------------------------------------------";;

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.setPlayerListFooter(footerText);
        }
    }

    public void updateTabListHeaderForAllPlayers() {
        String headerText = ChatColor.GRAY + "------------------------------------------";
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.setPlayerListHeader(headerText);
        }
    }

    public boolean createTeam(String teamName, Player leader) {
        if (teams.containsKey(teamName.toLowerCase())) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "A team with the name " + ACCENT_COLOR + teamName + ERROR_COLOR + " already exists.");
            return false;
        }
        if (teamName.length() > 10) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team name cannot be longer than 10 characters.");
            return true;
        }
        if (!teamName.matches("^[a-zA-Z0-9&_-]+$")) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team name contains invalid characters. Only alphanumeric and basic symbols are allowed.");
            return true;
        }
        if (getPlayerTeam(leader) != null) {
            leader.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are already in a team.");
            return false;
        }
        Team newTeam = new Team(teamName, leader);
        teams.put(teamName.toLowerCase(), newTeam);
        playerTeamMap.put(leader.getUniqueId(), teamName.toLowerCase());
        saveTeams();
        leader.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " created successfully!");
        updatePlayerTabName(leader);
        return true;
    }

    public boolean renameTeam(Team teamToRename, String newTeamName, Player requester) {
        if (teamToRename == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "An internal error occurred (team not found for rename).");
            return false;
        }
        if (!teamToRename.getOwner().equals(requester.getUniqueId())) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team owner can rename the team.");
            return false;
        }
        String oldTeamNameKey = teamToRename.getName().toLowerCase(); // This is the name before .setName() is called
        String newTeamNameKey = newTeamName.toLowerCase();

        if (teams.containsKey(newTeamNameKey)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "A team with the name '" + ACCENT_COLOR + newTeamName + ERROR_COLOR + "' already exists.");
            return false;
        }

        // Explicitly delete the old team entry from the data file BEFORE updating in-memory maps and saving.
        dataManager.deleteDataEntry(TEAMS_CONFIG_PATH, oldTeamNameKey);

        // Update the team's name object
        teamToRename.setName(newTeamName);

        // Update the main teams map (in-memory)
        teams.remove(oldTeamNameKey); // remove by old key
        teams.put(newTeamNameKey, teamToRename); // add with new key

        // Update the playerTeamMap for all members
        for (UUID memberId : teamToRename.getMembers()) {
            playerTeamMap.put(memberId, newTeamNameKey);
        }
        
        // Update pending invites if any team was inviting to this team by its old name
        List<UUID> playersToUpdateInvites = new ArrayList<>();
        pendingInvites.forEach((playerId, invitedTeamName) -> {
            if (invitedTeamName.equalsIgnoreCase(oldTeamNameKey)) {
                playersToUpdateInvites.add(playerId);
            }
        });
        for (UUID playerId : playersToUpdateInvites) {
            pendingInvites.remove(playerId, oldTeamNameKey);
            pendingInvites.put(playerId, newTeamNameKey);
        }

        saveTeams();
        // Notify team members about the name change (optional, but good UX)
        teamToRename.broadcastMessage(MSG_PREFIX + INFO_COLOR + "Your team has been renamed to '" + ACCENT_COLOR + newTeamName + INFO_COLOR + "' by " + ACCENT_COLOR + requester.getName() + INFO_COLOR + ".");
        updateAllPlayersTabNamesAndFooter();
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
        updateAllPlayersTabNamesAndFooter();
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
            team.broadcastMessage(INFO_COLOR + playerToAdd.getName() + ACCENT_COLOR + " has joined the team.");
            playerTeamMap.put(playerToAdd.getUniqueId(), teamName.toLowerCase());
            saveTeams(); // Save after adding member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + playerToAdd.getName() + " has been added to team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            playerToAdd.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You have joined team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            updatePlayerTabName(playerToAdd);
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
            team.broadcastMessage(INFO_COLOR + playerToRemove.getName() + ACCENT_COLOR + " has left or been kicked from the team.");
            saveTeams(); // Save after removing member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "You have been removed from team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            return true;
        } else if (!canKick) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "You do not have permission to remove this player.");
            return false;
        }

        if (team.removeMember(playerToRemove)) {
            playerTeamMap.remove(playerToRemove.getUniqueId());
            team.broadcastMessage(INFO_COLOR + playerToRemove.getName() + ACCENT_COLOR + " has left or been kicked from the team.");
            saveTeams(); // Save after removing member
            requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + playerToRemove.getName() + " has been removed from team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + ".");
            playerToRemove.sendMessage(MSG_PREFIX + INFO_COLOR + "You have been removed from team " + ACCENT_COLOR + teamName + INFO_COLOR + ".");
            updatePlayerTabName(playerToRemove);
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
        updateAllPlayersTabNamesAndFooter();
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
        updateAllPlayersTabNamesAndFooter();
        return true;
    }

    public boolean setTeamFriendlyFire(String teamName, boolean enabled, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        if (requesterRole != Team.Role.OWNER && requesterRole != Team.Role.MANAGER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER or MANAGER can change the friendly fire setting.");
            return false;
        }
        team.setFriendlyFireEnabled(enabled);
        saveTeams(); // Save after changing setting
        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Friendly fire for team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " has been " + (enabled ? "ENABLED" : "DISABLED") + ".");
        return true;
    }

    public boolean setTeamMotd(String teamName, String motd, Player requester) {
        Team team = getTeam(teamName);
        if (team == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Team " + ACCENT_COLOR + teamName + ERROR_COLOR + " not found.");
            return false;
        }
        Team.Role requesterRole = team.getPlayerRole(requester.getUniqueId());
        if (requesterRole != Team.Role.OWNER && requesterRole != Team.Role.MANAGER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team OWNER or MANAGER can set the MOTD.");
            return false;
        }
        team.setTeamMotd(motd);
        saveTeams(); // Save after changing setting
        String displayMotd = ChatColor.translateAlternateColorCodes('&', motd);
        requester.sendMessage(MSG_PREFIX + SUCCESS_COLOR + "Team " + ACCENT_COLOR + teamName + SUCCESS_COLOR + " MOTD set to: " + displayMotd + ChatColor.RESET);
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

    public void invitePlayer(Player invited, String teamName) {
        pendingInvites.put(invited.getUniqueId(), teamName);
    }

    public boolean hasInvite(UUID playerId, String teamName) {
        return pendingInvites.containsEntry(playerId, teamName);
    }

    public void removeInvite(UUID playerId, String teamName) {
        pendingInvites.remove(playerId, teamName);
    }

    public ArrayList<String> getPendingInvites(@NotNull UUID uniqueId) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return pendingInvites.get(uniqueId).stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveTeams() {
        Map<String, Map<String, Object>> teamsToSave = new HashMap<>();
        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            teamsToSave.put(entry.getKey(), entry.getValue().serialize());
        }
        dataManager.saveData(TEAMS_CONFIG_PATH, teamsToSave);
        plugin.getLogger().info("Saved " + teamsToSave.size() + " teams to " + TEAMS_DATA_FILE);
    }

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public boolean hasPermission(@NotNull UUID uniqueId, Team team, Team.Role role) {
        if (uniqueId == null || team == null || role == null) {
            throw new IllegalArgumentException("UUID, Team, and Role cannot be null");
        }
        Team.Role playerRole = team.getPlayerRole(uniqueId);
        return playerRole != null && (playerRole == Team.Role.OWNER || playerRole == role);
    }
}