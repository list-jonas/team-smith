package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamInfoHandler implements SubCommandExecutor {

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
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "--------------------");
    }

    @Override
    public String getArgumentUsage() {
        return ""; // No arguments needed
    }

    @Override
    public String getDescription() {
        return "Shows information about your current team.";
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        // No tab completions for info
        return Collections.emptyList();
    }
}