package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FriendlyFireHandler implements SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team friendlyfire " + getArgumentUsage());
            return true;
        }

        Team playerTeam = teamManager.getPlayerTeam(player);
        if (playerTeam == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }

        String toggle = args[0].toLowerCase();
        boolean enable;
        if (toggle.equals("on")) {
            enable = true;
        } else if (toggle.equals("off")) {
            enable = false;
        } else {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Invalid argument. Use 'on' or 'off'.");
            return true;
        }

        teamManager.setTeamFriendlyFire(playerTeam.getName(), enable, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<on|off>";
    }

    @Override
    public String getDescription() {
        return "Toggles friendly fire for your team. (Aliases: /team ff)";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        // Suggest 'on' and 'off' for the first argument
        if (args.length == 1) {
            return Arrays.asList("on", "off");
        }
        return Collections.emptyList();
    }
}