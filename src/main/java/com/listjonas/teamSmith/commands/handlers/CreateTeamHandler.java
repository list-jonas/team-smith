package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CreateTeamHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) { // args[0] is the subcommand itself, so we check for actual arguments
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team create " + getArgumentUsage());
            return true;
        }
        teamManager.createTeam(args[0], player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<teamName>";
    }

    @Override
    public String getDescription() {
        return "Creates a new team.";
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        // Suggest <teamName> as a placeholder for the required argument
        if (args.length == 1) {
            return Collections.singletonList("<teamName>");
        }
        return Collections.emptyList();
    }
}