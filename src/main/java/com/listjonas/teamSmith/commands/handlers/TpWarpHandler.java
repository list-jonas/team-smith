package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Collections;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class TpWarpHandler implements SubCommandExecutor {
    // Cooldown map: player UUID -> last warp time (ms)
    private static final Map<java.util.UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 3 * 60 * 1000; // 3 minutes

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
        long now = System.currentTimeMillis();
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed != null && now - lastUsed < COOLDOWN_MILLIS) {
            long secondsLeft = (COOLDOWN_MILLIS - (now - lastUsed)) / 1000;
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You must wait " + secondsLeft + " seconds before using a team warp again.");
            return true;
        }
        cooldowns.put(player.getUniqueId(), now);
        player.teleport(warp);
        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "Teleported to warp '" + warpName + "'.");
        return true;
    }
    @Override
    public String getArgumentUsage() { return "<name>"; }
    @Override
    public String getDescription() { return "Teleport to a named team warp (3 min cooldown)."; }
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
}