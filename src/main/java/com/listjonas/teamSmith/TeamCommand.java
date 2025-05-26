package com.listjonas.teamSmith;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamSmith plugin;
    private final TeamManager teamManager;

    public TeamCommand(TeamSmith plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("Usage: /team create <teamName>");
                    return true;
                }
                teamManager.createTeam(args[1], player);
                break;
            case "delete":
            case "disband":
                Team playerTeamForDelete = teamManager.getPlayerTeam(player);
                if (playerTeamForDelete == null) {
                    player.sendMessage("You are not in a team to delete/disband.");
                    return true;
                }
                teamManager.deleteTeam(playerTeamForDelete.getName(), player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("Usage: /team invite <playerName>");
                    return true;
                }
                Player targetInvite = Bukkit.getPlayer(args[1]);
                if (targetInvite == null) {
                    player.sendMessage("Player '" + args[1] + "' not found.");
                    return true;
                }
                Team teamToInvite = teamManager.getPlayerTeam(player);
                if (teamToInvite == null) {
                    player.sendMessage("You are not in a team to invite players to.");
                    return true;
                }
                // For simplicity, directly adding. Consider an actual invite/accept system.
                teamManager.addPlayerToTeam(teamToInvite.getName(), targetInvite, player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage("Usage: /team kick <playerName>");
                    return true;
                }
                Player targetKick = Bukkit.getPlayer(args[1]);
                if (targetKick == null) {
                    player.sendMessage("Player '" + args[1] + "' not found.");
                    return true;
                }
                Team teamToKickFrom = teamManager.getPlayerTeam(player);
                if (teamToKickFrom == null) {
                    player.sendMessage("You are not in a team to kick players from.");
                    return true;
                }
                teamManager.removePlayerFromTeam(teamToKickFrom.getName(), targetKick, player);
                break;
            case "leave":
                Team teamToLeave = teamManager.getPlayerTeam(player);
                if (teamToLeave == null) {
                    player.sendMessage("You are not in a team to leave.");
                    return true;
                }
                teamManager.removePlayerFromTeam(teamToLeave.getName(), player, player);
                break;
            case "transfer":
                if (args.length < 2) {
                    player.sendMessage("Usage: /team transfer <newLeaderName>");
                    return true;
                }
                Player newLeader = Bukkit.getPlayer(args[1]);
                if (newLeader == null) {
                    player.sendMessage("Player '" + args[1] + "' not found.");
                    return true;
                }
                Team teamToTransfer = teamManager.getPlayerTeam(player);
                if (teamToTransfer == null) {
                    player.sendMessage("You are not in a team to transfer leadership.");
                    return true;
                }
                teamManager.transferLeadership(teamToTransfer.getName(), newLeader, player);
                break;
            case "prefix":
                if (args.length < 2) {
                    player.sendMessage("Usage: /team prefix <newPrefix>");
                    return true;
                }
                Team teamForPrefix = teamManager.getPlayerTeam(player);
                if (teamForPrefix == null) {
                    player.sendMessage("You are not in a team to set a prefix for.");
                    return true;
                }
                // Combine remaining args for prefix with spaces
                String prefix = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                teamManager.setTeamPrefix(teamForPrefix.getName(), prefix, player);
                break;
            case "info":
                Team currentTeam = teamManager.getPlayerTeam(player);
                if (currentTeam == null) {
                    player.sendMessage("You are not currently in a team.");
                    return true;
                }
                sendTeamInfo(player, currentTeam);
                break;
            default:
                sendHelpMessage(player);
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("--- TeamSmith Help ---");
        player.sendMessage("/team create <teamName> - Creates a new team.");
        player.sendMessage("/team delete - Deletes your current team (leader only).");
        player.sendMessage("/team invite <playerName> - Invites a player to your team.");
        player.sendMessage("/team kick <playerName> - Kicks a player from your team (leader only).");
        player.sendMessage("/team leave - Leaves your current team.");
        player.sendMessage("/team transfer <newLeaderName> - Transfers leadership (leader only).");
        player.sendMessage("/team prefix <newPrefix> - Sets your team's chat prefix (leader only).");
        player.sendMessage("/team info - Shows information about your current team.");
    }

    private void sendTeamInfo(Player player, Team team) {
        player.sendMessage("--- Team: " + team.getName() + " ---");
        Player leader = Bukkit.getPlayer(team.getLeader());
        player.sendMessage("Leader: " + (leader != null ? leader.getName() : "Unknown (Offline)"));
        player.sendMessage("Prefix: " + team.getPrefix());
        player.sendMessage("Members (" + team.getSize() + "):");
        for (UUID memberId : team.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            player.sendMessage("- " + (member != null ? member.getName() : "Unknown (Offline)"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("create", "delete", "invite", "kick", "leave", "transfer", "prefix", "info");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("invite") || subCommand.equals("kick") || subCommand.equals("transfer")) {
                // Suggest online players
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            }
        }
        return completions;
    }
}