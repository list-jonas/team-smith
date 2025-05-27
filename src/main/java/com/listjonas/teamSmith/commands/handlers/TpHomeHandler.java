package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Collections;
import java.util.List;

public class TpHomeHandler extends SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Location home = team.getHomeLocation();
        if (home == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "No team home is set.");
            return true;
        }
        player.teleport(home);
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Teleported to team home.");
        return true;
    }

    @Override
    public String getDescription() { return "Teleport to the team home location."; }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }
}