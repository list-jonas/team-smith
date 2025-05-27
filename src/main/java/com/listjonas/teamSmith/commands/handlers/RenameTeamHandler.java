package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RenameTeamHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team rename <newTeamName>");
            return true;
        }

        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }

        if (!team.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Only the team owner can rename the team.");
            return true;
        }

        String newTeamName = args[0];

        // Validate new team name (e.g., length, characters, uniqueness)
        if (newTeamName.length() < 3 || newTeamName.length() > 16) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Team name must be between 3 and 16 characters.");
            return true;
        }
        // Add more validation if needed (e.g., allowed characters using regex)
        // if (!newTeamName.matches("^[a-zA-Z0-9_]+$")) {
        //     player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Team name can only contain letters, numbers, and underscores.");
        //     return true;
        // }

        if (teamManager.getTeam(newTeamName) != null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "A team with the name '" + TeamCommand.ACCENT_COLOR + newTeamName + TeamCommand.ERROR_COLOR + "' already exists.");
            return true;
        }

        boolean success = teamManager.renameTeam(team, newTeamName, player);
        if (success) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Team renamed to '" + TeamCommand.ACCENT_COLOR + newTeamName + TeamCommand.SUCCESS_COLOR + "'.");
        } else {
            // The renameTeam method in TeamManager should handle specific error messages
            // or this handler can provide a generic one if renameTeam returns false for other reasons.
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Could not rename the team. Please try again or contact an admin.");
        }

        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<newTeamName>";
    }

    @Override
    public String getDescription() {
        return "Renames your team (owner only).";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.OWNER;
    }

}