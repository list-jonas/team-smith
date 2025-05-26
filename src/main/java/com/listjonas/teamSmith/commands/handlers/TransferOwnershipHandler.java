package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TransferOwnershipHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team transfer " + getArgumentUsage());
            return true;
        }
        Player newLeader = Bukkit.getPlayer(args[0]);
        if (newLeader == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' not found.");
            return true;
        }
        Team teamToTransfer = teamManager.getPlayerTeam(player);
        if (teamToTransfer == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to transfer leadership.");
            return true;
        }
        teamManager.transferLeadership(teamToTransfer.getName(), newLeader, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<newLeaderName>";
    }

    @Override
    public String getDescription() {
        return "Transfers ownership (OWNER only).";
    }
}