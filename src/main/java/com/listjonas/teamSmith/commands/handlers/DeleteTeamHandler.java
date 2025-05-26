package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

public class DeleteTeamHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team playerTeamForDelete = teamManager.getPlayerTeam(player);
        if (playerTeamForDelete == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to delete/disband.");
            return true;
        }
        teamManager.deleteTeam(playerTeamForDelete.getName(), player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return ""; // No arguments needed
    }

    @Override
    public String getDescription() {
        return "Deletes your current team (OWNER only).";
    }
}