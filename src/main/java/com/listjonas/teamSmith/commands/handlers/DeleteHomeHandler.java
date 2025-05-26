package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class DeleteHomeHandler implements SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Team.Role role = team.getPlayerRole(player.getUniqueId());
        if (role != Team.Role.OWNER && role != Team.Role.MANAGER) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Only OWNER or MANAGER can delete the team home.");
            return true;
        }
        if (team.getHomeLocation() == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "No team home is set.");
            return true;
        }
        team.deleteHomeLocation();
        teamManager.saveTeams();
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Team home deleted.");
        return true;
    }
    @Override
    public String getArgumentUsage() { return ""; }
    @Override
    public String getDescription() { return "Deletes the team home location (OWNER/MANAGER only)."; }
    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) { return Collections.emptyList(); }
}