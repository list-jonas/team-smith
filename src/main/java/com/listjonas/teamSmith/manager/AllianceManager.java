package com.listjonas.teamSmith.manager;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.data.DataManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

import java.util.*;

import static com.listjonas.teamSmith.commands.TeamCommand.*;

public class AllianceManager {
    private final TeamSmith plugin;
    private final TeamManager teamManager;
    private final Map<String, Set<String>> teamAlliances; // Maps team name to set of allied team names
    private final DataManager dataManager;
    private static final String ALLIANCES_DATA_FILE = "alliances.yml";
    private static final String ALLIANCES_CONFIG_PATH = "alliances";
    private static AllianceManager instance;

    private AllianceManager(TeamSmith plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.teamAlliances = new HashMap<>();
        this.dataManager = new DataManager(plugin, ALLIANCES_DATA_FILE);
        loadAlliances();
    }

    public static AllianceManager createInstance(TeamSmith plugin, TeamManager teamManager) {
        if (instance == null) {
            instance = new AllianceManager(plugin, teamManager);
        }
        return instance;
    }

    public static AllianceManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AllianceManager has not been initialized. Call createInstance() first.");
        }
        return instance;
    }

    private void loadAlliances() {
        // Load all team names that have alliances
        Map<String, Map<String, Object>> allAllianceData = dataManager.loadData(ALLIANCES_CONFIG_PATH);
        if (allAllianceData != null) {
            for (String teamName : allAllianceData.keySet()) {
                List<String> alliesList = dataManager.loadStringList(ALLIANCES_CONFIG_PATH + "." + teamName);
                if (alliesList != null && !alliesList.isEmpty()) {
                    teamAlliances.put(teamName.toLowerCase(), new HashSet<>(alliesList));
                }
            }
        }
    }

    public void saveAlliances() {
        // Clear existing alliances in config to prevent stale data
        dataManager.getConfig().set(ALLIANCES_CONFIG_PATH, null);

        for (Map.Entry<String, Set<String>> entry : teamAlliances.entrySet()) {
            String teamName = entry.getKey();
            List<String> alliesList = new ArrayList<>(entry.getValue());
            dataManager.saveStringList(ALLIANCES_CONFIG_PATH + "." + teamName, alliesList);
        }
    }

    public boolean createAlliance(String teamName, String allyTeamName, Player requester) {
        Team team = teamManager.getTeam(teamName);
        Team allyTeam = teamManager.getTeam(allyTeamName);

        if (team == null || allyTeam == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "One or both teams not found.");
            return false;
        }

        if (team.getPlayerRole(requester.getUniqueId()) != Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team owner can create alliances.");
            return false;
        }

        String teamKey = teamName.toLowerCase();
        String allyTeamKey = allyTeamName.toLowerCase();

        if (teamKey.equals(allyTeamKey)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "A team cannot ally with itself.");
            return false;
        }

        teamAlliances.computeIfAbsent(teamKey, k -> new HashSet<>()).add(allyTeamKey);
        saveAlliances();

        team.broadcastMessage(INFO_COLOR + " Your team is now allied with " + ACCENT_COLOR + allyTeamName + INFO_COLOR + "!");
        allyTeam.broadcastMessage(INFO_COLOR + " Team " + ACCENT_COLOR + teamName + INFO_COLOR + " has allied with your team!");
        return true;
    }

    public boolean removeAlliance(String teamName, String allyTeamName, Player requester) {
        Team team = teamManager.getTeam(teamName);
        Team allyTeam = teamManager.getTeam(allyTeamName);

        if (team == null || allyTeam == null) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "One or both teams not found.");
            return false;
        }

        if (team.getPlayerRole(requester.getUniqueId()) != Team.Role.OWNER) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only the team owner can remove alliances.");
            return false;
        }

        String teamKey = teamName.toLowerCase();
        String allyTeamKey = allyTeamName.toLowerCase();

        Set<String> allies = teamAlliances.get(teamKey);
        if (allies == null || !allies.contains(allyTeamKey)) {
            requester.sendMessage(MSG_PREFIX + ERROR_COLOR + "Your team is not allied with " + ACCENT_COLOR + allyTeamName + ERROR_COLOR + ".");
            return false;
        }

        allies.remove(allyTeamKey);
        if (allies.isEmpty()) {
            teamAlliances.remove(teamKey);
        }
        saveAlliances();

        team.broadcastMessage(INFO_COLOR + " Your team is no longer allied with " + ACCENT_COLOR + allyTeamName + INFO_COLOR + ".");
        allyTeam.broadcastMessage(INFO_COLOR + " Team " + ACCENT_COLOR + teamName + INFO_COLOR + " has removed their alliance with your team.");
        return true;
    }

    public boolean areTeamsAllied(String team1Name, String team2Name) {
        Set<String> team1Allies = teamAlliances.get(team1Name.toLowerCase());
        return team1Allies != null && team1Allies.contains(team2Name.toLowerCase());
    }

    public Set<String> getAllies(String teamName) {
        return teamAlliances.getOrDefault(teamName.toLowerCase(), new HashSet<>());
    }

    public Set<String> getTeamsAlliedWith(String teamName) {
        Set<String> alliedWith = new HashSet<>();
        String lowerCaseTeamName = teamName.toLowerCase();
        for (Map.Entry<String, Set<String>> entry : teamAlliances.entrySet()) {
            if (entry.getValue().contains(lowerCaseTeamName)) {
                alliedWith.add(entry.getKey());
            }
        }
        return alliedWith;
    }
}