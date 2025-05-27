package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeamListHandler implements SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Collection<Team> teams = teamManager.getAllTeams();
        
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "--- Team List ---");
        if (teams.isEmpty()) {
            player.sendMessage(TeamCommand.INFO_COLOR + "No teams found");
        } else {
            teams.forEach(team -> {
                UUID ownerId = team.getOwner();
                Player owner = Bukkit.getPlayer(ownerId);
                String ownerName = owner != null ? owner.getName() : "Offline Player";
                String ideology = team.getIdeology();
                String ideologyDisplay = (ideology != null && !ideology.isEmpty()) ? TeamCommand.INFO_COLOR + ", Ideology: " + TeamCommand.ACCENT_COLOR + ideology : "";
                player.sendMessage(TeamCommand.ACCENT_COLOR + team.getName() + 
                    TeamCommand.INFO_COLOR + " - Owner: " + ownerName +
                    ", Members: " + team.getSize() + ideologyDisplay);
            });
        }
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "----------------");
        return true;
    }

    @Override
    public String getDescription() {
        return "List all existing teams";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getArgumentUsage() {
        return "";
    }
}