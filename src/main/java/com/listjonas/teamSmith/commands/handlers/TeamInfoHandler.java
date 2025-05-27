package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TeamInfoHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team currentTeam = teamManager.getPlayerTeam(player);
        if (currentTeam == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "You are not currently in a team.");
            return true;
        }
        sendTeamInfo(player, currentTeam);
        return true;
    }

    private void sendTeamInfo(Player player, Team team) {
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "--- Team: " + team.getName() + " ---");
        UUID ownerId = team.getOwner();
        Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
        player.sendMessage(TeamCommand.INFO_COLOR + "Owner: " + TeamCommand.ACCENT_COLOR + (owner != null ? owner.getName() : (ownerId != null ? "Offline UUID: " + ownerId.toString().substring(0,8) : "Unknown")));
        
        String prefixText = team.getPrefix() == null || team.getPrefix().isEmpty() ? "Not set" : team.getPrefix();
        String prefixColor = team.getPrefixColor();
        String displayPrefix = ChatColor.translateAlternateColorCodes('&', prefixColor + prefixText) + ChatColor.RESET;
        player.sendMessage(TeamCommand.INFO_COLOR + "Prefix: " + displayPrefix + (prefixText.equals("Not set") ? "" : TeamCommand.INFO_COLOR + " (Raw: " + TeamCommand.ACCENT_COLOR + prefixColor + prefixText + TeamCommand.INFO_COLOR + ")"));
        
        String ideology = team.getIdeology();
        if (ideology != null && !ideology.isEmpty()) {
            player.sendMessage(TeamCommand.INFO_COLOR + "Ideology: " + TeamCommand.ACCENT_COLOR + ideology);
        }

        player.sendMessage(TeamCommand.INFO_COLOR + "Members (" + TeamCommand.ACCENT_COLOR + team.getSize() + TeamCommand.INFO_COLOR + "):");

        Map<UUID, Team.Role> memberRoles = team.getMemberRoles();
        List<UUID> sortedMembers = new ArrayList<>(memberRoles.keySet());
        // Sort members to show Owner first, then Managers, then Members
        sortedMembers.sort(Comparator.comparing((UUID id) -> memberRoles.get(id)).reversed());

        for (UUID memberId : sortedMembers) {
            Player member = Bukkit.getPlayer(memberId);
            Team.Role role = memberRoles.get(memberId);
            String roleString = TeamCommand.ACCENT_COLOR + "(" + role.name() + ")";
            player.sendMessage(ChatColor.GRAY + "- " + TeamCommand.ACCENT_COLOR + (member != null ? member.getName() : "Offline UUID: " + memberId.toString().substring(0,8)) + " " + roleString);
        }

        String motd = team.getTeamMotd();
        if (motd != null && !motd.isEmpty()) {
            player.sendMessage(TeamCommand.INFO_COLOR + "MOTD: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', motd));
        }

        // Server Memory Usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = allocatedMemory - freeMemory;

        player.sendMessage(TeamCommand.MSG_PREFIX + ChatColor.GOLD + "--- Server Vitals ---");
        player.sendMessage(TeamCommand.INFO_COLOR + "Used Memory: " + TeamCommand.ACCENT_COLOR + usedMemory + "MB / " + allocatedMemory + TeamCommand.INFO_COLOR + "MB allocated");
        player.sendMessage(TeamCommand.INFO_COLOR + "Max Memory: " + TeamCommand.ACCENT_COLOR + maxMemory + "MB");

        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "--------------------");
    }

    @Override
    public String getDescription() {
        return "Shows information about your current team.";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }

    @Override
    public List<String> getTabCompletions(org.bukkit.command.CommandSender sender, String[] args) {
        // No tab completions for info
        return java.util.Collections.emptyList();
    }
}