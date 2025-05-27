package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class DeleteWarpHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team delwarp <name>");
            return true;
        }
        String warpName = args[0];
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Team.Role role = team.getPlayerRole(player.getUniqueId());
        if (role != Team.Role.OWNER && role != Team.Role.MANAGER) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Only OWNER or MANAGER can delete warps.");
            return true;
        }
        if (team.deleteWarp(warpName)) {
            teamManager.saveTeams();
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Warp '" + warpName + "' deleted.");
        } else {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Warp '" + warpName + "' not found.");
        }
        return true;
    }
    @Override
    public String getArgumentUsage() { return "<name>"; }

    @Override
    public String getDescription() { return "Deletes a named team warp (OWNER/MANAGER)."; }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Suggest available warp names for the player's team
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Team team = TeamManager.getInstance().getPlayerTeam(player);
                if (team != null) {
                    return new java.util.ArrayList<>(team.getWarps().keySet());
                }
            }
            return java.util.Collections.emptyList();
        }
        return java.util.Collections.emptyList();
    }
}