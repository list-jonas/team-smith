package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.AllianceManager;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.listjonas.teamSmith.commands.TeamCommand.*;

public class AllyHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 2) {
            player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Usage: /team ally <add|remove> <team>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String targetTeamName = args[1];
        String playerTeamName = teamManager.getPlayerTeam(player).getName();

        switch (subCommand) {
            case "add":
                return AllianceManager.getInstance().createAlliance(playerTeamName, targetTeamName, player);
            case "remove":
                return AllianceManager.getInstance().removeAlliance(playerTeamName, targetTeamName, player);
            default:
                player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Usage: /team ally <add|remove> <team>");
                return true;
        }
    }

    @Override
    public List<String> getTabCompletions(org.bukkit.command.CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        TeamManager teamManager = TeamManager.getInstance();
        AllianceManager allianceManager = AllianceManager.getInstance();
        String playerTeamName = teamManager.getPlayerTeam(player).getName();

        if (args.length == 1) {
            return Arrays.asList("add", "remove").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("add".equals(subCommand)) {
                // Suggest all team names except the player's own team and already allied teams
                return teamManager.getAllTeams().stream()
                        .filter(team -> !team.getName().equalsIgnoreCase(playerTeamName) &&
                                !allianceManager.getAllies(playerTeamName).contains(team.getName().toLowerCase()))
                        .map(team -> team.getName())
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ("remove".equals(subCommand)) {
                // Suggest only currently allied teams
                return allianceManager.getAllies(playerTeamName).stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Manage team alliances (add/remove).";
    }

    @Override
    public String getArgumentUsage() {
        return "<add|remove> <team>";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.OWNER;
    }
}