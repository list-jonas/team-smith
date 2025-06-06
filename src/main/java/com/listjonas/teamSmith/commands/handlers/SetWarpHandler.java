package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.util.DebugUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Collections;
import java.util.List;

public class SetWarpHandler extends SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team setwarp <name>");
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
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Only OWNER or MANAGER can set warps.");
            return true;
        }
        if (warpName.length() > 16) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Warp name too long (max 16 chars).");
            return true;
        }
        Location loc = player.getLocation();
        int maxWarps = DebugUtil.isDebugPlayer(player.getUniqueId()) ? Integer.MAX_VALUE : TeamSmith.getInstance().getConfigData().getMaxWarps();
        boolean success = team.setWarp(warpName, loc, maxWarps);
        if (!success) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You can only have up to " + maxWarps + " warps.");
            return true;
        }
        teamManager.saveTeams();
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Warp '" + warpName + "' set to your current location.");
        return true;
    }
    
    @Override
    public String getArgumentUsage() { return "<name>"; }
    
    @Override
    public String getDescription() { return "Sets a named team warp (OWNER/MANAGER, max 3)."; }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }
    
    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) return Collections.singletonList("<name>");
        return Collections.emptyList();
    }
}