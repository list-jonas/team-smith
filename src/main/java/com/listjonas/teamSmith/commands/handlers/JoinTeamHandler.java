package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JoinTeamHandler implements SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length != 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX
                    + TeamCommand.INFO_COLOR + "Usage: /team join <teamName>");
            return true;
        }
        String teamName = args[0];
        // 1) check invite
        if (!teamManager.hasInvite(player.getUniqueId(), teamName)) {
            player.sendMessage(TeamCommand.MSG_PREFIX
                    + TeamCommand.ERROR_COLOR + "You have no invite to '"
                    + TeamCommand.ACCENT_COLOR + teamName + TeamCommand.ERROR_COLOR + "'.");
            return true;
        }
        // 2) add to team
        boolean ok = teamManager.addPlayerToTeam(teamName, player, player);
        if (ok) {
            teamManager.removeInvite(player.getUniqueId(), teamName);
        }
        return true;
    }
    @Override public String getArgumentUsage() { return "<teamName>"; }
    @Override public String getDescription() { return "Accept a team invitation."; }
    @Override public List<String> getTabCompletions(CommandSender s, String[] a) {
        if (a.length == 1 && s instanceof Player) {
            return TeamManager.getInstance().getPendingInvites(((Player)s).getUniqueId())
                    .stream().filter(t->t.startsWith(a[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

