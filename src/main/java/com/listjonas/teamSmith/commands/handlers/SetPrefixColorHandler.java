package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetPrefixColorHandler extends SubCommandExecutor {

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team setprefixcolor " + getArgumentUsage());
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Example: /team setprefixcolor &c (for red)");
            return true;
        }
        Team teamForPrefixColor = teamManager.getPlayerTeam(player);
        if (teamForPrefixColor == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team to set a prefix color for.");
            return true;
        }
        String colorCode = args[0];
        teamManager.setTeamPrefixColor(teamForPrefixColor.getName(), colorCode, player);
        return true;
    }

    @Override
    public String getArgumentUsage() {
        return "<colorCode>";
    }

    @Override
    public String getDescription() {
        return "Sets your team's prefix color (OWNER/MANAGER only).";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MANAGER;
    }

    @Override
    public List<String> getTabCompletions(org.bukkit.command.CommandSender sender, String[] args) {
        // Suggest Minecraft color codes for the first argument
        if (args.length == 1) {
            return Arrays.asList("&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
                                 "&k", "&l", "&m", "&n", "&o", "&r");
        }
        return Collections.emptyList();
    }
}