package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

public class SetPrefixColorHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team prefixcolor " + getArgumentUsage());
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Example: /team prefixcolor &c (for red)");
            return true;
        }
        Team teamForPrefixColor = teamManager.getPlayerTeam(player);
        if (teamForPrefixColor == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to set a prefix color for.");
            return true;
        }
        String colorCode = args[0];
        teamManager.setTeamPrefixColor(teamForPrefixColor.getName(), colorCode, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<colorCode>";
    }

    @Override
    public String getDescription() {
        return "Sets your team's prefix color (OWNER/MANAGER only).";
    }
}