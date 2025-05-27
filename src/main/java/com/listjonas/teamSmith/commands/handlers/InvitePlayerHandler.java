package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InvitePlayerHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team invite " + getArgumentUsage());
            return true;
        }
        Player targetInvite = Bukkit.getPlayer(args[0]);
        if (targetInvite == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' not found.");
            return true;
        }

        // Edge case: Player trying to invite themselves
        if (targetInvite.equals(player)) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You cannot invite yourself to your team.");
            return true;
        }

        Team teamToInvite = teamManager.getPlayerTeam(player);
        if (teamToInvite == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to invite players to.");
            return true;
        }

        // Edge case: Player already in a team (or this team)
        if (teamManager.getPlayerTeam(targetInvite) != null) {
            if (teamManager.getPlayerTeam(targetInvite).equals(teamToInvite)) {
                player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + targetInvite.getName() + " is already in your team.");
            } else {
                player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + targetInvite.getName() + " is already in another team.");
            }
            return true;
        }

        String teamName = teamToInvite.getName(); // Use the validated teamToInvite

        // 1) register the invite
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Player '" + TeamCommand.ACCENT_COLOR + args[0] + TeamCommand.ERROR_COLOR + "' is not online.");
            return true;
        }
        teamManager.invitePlayer(targetInvite, teamName); // Use targetInvite consistently

        Component header = Component.text(TeamCommand.MSG_PREFIX)
                .color(NamedTextColor.GOLD)
                .append(Component.text(player.getName(), NamedTextColor.AQUA))
                .append(Component.text(" has invited you to join team ", NamedTextColor.YELLOW))
                .append(Component.text(teamName, NamedTextColor.AQUA))
                .append(Component.text(".", NamedTextColor.YELLOW));

        target.sendMessage(header);

        // 2) the clickable “[Click here to JOIN]” button
        Component joinButton = Component.text("[Click here to JOIN]")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/team join " + teamName))
                .hoverEvent(HoverEvent.showText(Component.text("Accept the invitation")));

        // Send it (you can send multiple components in one call)
        target.sendMessage(joinButton);

        // confirmation back to inviter
        player.sendMessage(
                TeamCommand.MSG_PREFIX
                        + TeamCommand.SUCCESS_COLOR + "Invitation sent to "
                        + TeamCommand.ACCENT_COLOR + target.getName()
        );

        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<playerName>";
    }

    @Override
    public String getDescription() {
        return "Invites a player to your team.";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Suggest online player names for the first argument, excluding the sender if they are a player
        if (args.length == 1) {
            String currentPlayerName = "";
            if (sender instanceof Player) {
                currentPlayerName = sender.getName();
            }
            final String finalCurrentPlayerName = currentPlayerName; // effectively final for lambda
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equalsIgnoreCase(finalCurrentPlayerName)) // Exclude current player
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}