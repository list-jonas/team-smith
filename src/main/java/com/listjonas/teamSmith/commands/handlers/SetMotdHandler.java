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

public class SetMotdHandler extends SubCommandExecutor {

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

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Suggest <motd> as a placeholder for the required argument
        if (args.length == 1) {
            return Collections.singletonList("<motd>");
        }
        return Collections.emptyList();
    }
}