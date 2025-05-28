package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.*;

public class TpWarpHandler extends SubCommandExecutor {
    // Cooldown map: player UUID -> last warp time (ms)
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    private int warpTimeoutSeconds;

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length < 1) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Usage: /team warp <name>");
            return true;
        }
        String warpName = args[0];
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Location warp = team.getWarp(warpName);
        if (warp == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Warp '" + warpName + "' not found.");
            return true;
        }
        // Get warp timeout from config
        this.warpTimeoutSeconds = TeamSmith.getInstance().getConfigData().getWarpTimeoutSeconds();


        // Check cooldown
        // Check for testing purposes
        long lastWarpTime = player.getUniqueId().toString().equals("bb2e57e7-28c2-4208-99b7-e724342f0596") ? 0 : cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeLeft = (lastWarpTime + (warpTimeoutSeconds * 1000)) - System.currentTimeMillis();

        if (timeLeft > 0) {
            long secondsLeft = timeLeft / 1000;
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You must wait " + secondsLeft + " seconds before using a team warp again.");
            return true;
        }
        // Apply cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.teleport(warp);
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Teleported to warp '" + warpName + "'.");
        return true;
    }

    @Override
    public String getArgumentUsage() { return "<name>"; }

    @Override
    public String getDescription() { return "Teleport to a named team warp (configurable cooldown)."; }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Suggest available warp names for the player's team
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Team team = TeamManager.getInstance().getPlayerTeam(player);
                if (team != null) {
                    return new java.util.ArrayList<>(team.getWarps().keySet());
                }
            }
            return java.util.Collections.emptyList();
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }
}