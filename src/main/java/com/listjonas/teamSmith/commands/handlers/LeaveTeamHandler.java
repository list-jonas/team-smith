package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class LeaveTeamHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team teamToLeave = teamManager.getPlayerTeam(player);
        if (teamToLeave == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to leave.");
            return true;
        }
        teamManager.removePlayerFromTeam(teamToLeave.getName(), player, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return ""; // No arguments needed
    }

    @Override
    public String getDescription() {
        return "Leaves your current team.";
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        // No tab completions for leave
        return Collections.emptyList();
    }
}