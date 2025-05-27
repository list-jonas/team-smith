package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.listjonas.teamSmith.commands.TeamCommand.*;

public class SetIdeologyHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team team = teamManager.getPlayerTeam(player);

        if (team == null) {
            player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team.");
            return true;
        }

        if (!teamManager.hasPermission(player.getUniqueId(), team, Team.Role.MANAGER)) {
            player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You do not have permission to set the team ideology. Only Managers or Owners can.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Usage: /team setideology <ideology text>");
            return true;
        }

        String ideology = String.join(" ", args);
        if (ideology.length() > 100) {
            player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Ideology text is too long (max 100 characters).");
            return true;
        }

        team.setIdeology(ideology);
        teamManager.saveTeams();

        team.broadcastMessage(INFO_COLOR + "Team ideology has been set to: '" + ACCENT_COLOR + ideology + INFO_COLOR + "' by " + ACCENT_COLOR + player.getName() + INFO_COLOR + ".");
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Sets the team's ideology (MANAGER/OWNER only).";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return args.length == 1
            ? Collections.singletonList("<ideology text>")
            : Collections.emptyList();
    }
}