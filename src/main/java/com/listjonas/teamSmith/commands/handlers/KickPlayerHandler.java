package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KickPlayerHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team kick " + getArgumentUsage());
            return true;
        }
        Player targetKick = Bukkit.getPlayer(args[0]);
        if (targetKick == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' not found.");
            return true;
        }
        Team teamToKickFrom = teamManager.getPlayerTeam(player);
        if (teamToKickFrom == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to kick players from.");
            return true;
        }
        teamManager.removePlayerFromTeam(teamToKickFrom.getName(), targetKick, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<playerName>";
    }

    @Override
    public String getDescription() {
        return "Kicks a player from your team (OWNER/MANAGER only).";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Suggest online player names for the first argument
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}