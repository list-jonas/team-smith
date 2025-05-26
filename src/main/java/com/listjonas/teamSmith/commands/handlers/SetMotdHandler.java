package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SetMotdHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team motd " + getArgumentUsage());
            return true;
        }
        Team teamForMotd = teamManager.getPlayerTeam(player);
        if (teamForMotd == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to set an MOTD for.");
            return true;
        }
        String motd = String.join(" ", args);
        teamManager.setTeamMotd(teamForMotd.getName(), motd, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<message>";
    }

    @Override
    public String getDescription() {
        return "Sets the team's Message of the Day (OWNER/MANAGER only).";
    }
}