package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetRoleHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 2) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team setrole " + getArgumentUsage());
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Available roles: " + TeamCommand.ACCENT_COLOR + "MANAGER, MEMBER");
            return true;
        }
        Team teamForSetRole = teamManager.getPlayerTeam(player);
        if (teamForSetRole == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Player targetPlayerSetRole = Bukkit.getPlayer(args[0]);
        if (targetPlayerSetRole == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' not found.");
            return true;
        }
        Team.Role newRole;
        try {
            newRole = Team.Role.valueOf(args[1].toUpperCase());
            if (newRole == Team.Role.OWNER) {
                player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Cannot set role to OWNER using this command. Use /team transfer.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Invalid role '" + TeamCommand.ACCENT_COLOR + args[1] + TeamCommand.ERROR_COLOR + "'. Available roles: " + TeamCommand.ACCENT_COLOR + "MANAGER, MEMBER");
            return true;
        }
        teamManager.promotePlayerRole(teamForSetRole.getName(), targetPlayerSetRole, newRole, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<playerName> <role>";
    }

    @Override
    public String getDescription() {
        return "Sets a player's role (OWNER only). Roles: MANAGER, MEMBER.";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.OWNER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Suggest online player names for the first argument
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest roles for the second argument
            return Arrays.asList("MANAGER", "MEMBER");
        }
        return Collections.emptyList();
    }
}