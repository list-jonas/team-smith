package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InvitePlayerHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team invite " + getArgumentUsage());
            return true;
        }
        Player targetInvite = Bukkit.getPlayer(args[0]);
        if (targetInvite == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' not found.");
            return true;
        }
        Team teamToInvite = teamManager.getPlayerTeam(player);
        if (teamToInvite == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to invite players to.");
            return true;
        }
        teamManager.addPlayerToTeam(teamToInvite.getName(), targetInvite, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<playerName>";
    }

    @Override
    public String getDescription() {
        return "Invites a player to your team.";
    }
}