package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetPrefixHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team setprefix " + getArgumentUsage());
            return true;
        }
        Team teamForPrefix = teamManager.getPlayerTeam(player);
        if (teamForPrefix == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to set a prefix for.");
            return true;
        }
        String prefix = String.join(" ", args);

        // Validate prefix: no spaces, max 10 chars, only alphanumeric and basic symbols
        if (prefix.contains(" ")) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Prefix cannot contain spaces.");
            return true;
        }
        if (prefix.length() > 10) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Prefix cannot be longer than 10 characters.");
            return true;
        }
        if (!prefix.matches("^[a-zA-Z0-9&_-]+$")) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Prefix contains invalid characters. Only alphanumeric and basic symbols are allowed.");
            return true;
        }

        teamManager.setTeamPrefix(teamForPrefix.getName(), prefix, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<newPrefix>";
    }

    @Override
    public String getDescription() {
        return "Sets your team's chat prefix (OWNER/MANAGER only).";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Suggest <prefix> as a placeholder for the required argument
        if (args.length == 1) {
            return Collections.singletonList("<prefix>");
        }
        return Collections.emptyList();
    }
}