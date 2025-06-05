package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.manager.AllianceManager;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.util.TeleportUtil;
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
        
        Team playerTeam = teamManager.getPlayerTeam(player);
        if (playerTeam == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        
        Location warp = null;
        Team targetTeam = null;

        // First, check player's own team warps
        warp = playerTeam.getWarp(warpName);
        if (warp != null) {
            targetTeam = playerTeam;
        } else {
            // If not found in own team, check allied teams
            Set<String> teamsAlliedWithPlayerTeam = AllianceManager.getInstance().getTeamsAlliedWith(playerTeam.getName());
            for (String alliedTeamName : teamsAlliedWithPlayerTeam) {
                Team alliedTeam = teamManager.getTeam(alliedTeamName);
                if (alliedTeam != null) {
                    warp = alliedTeam.getWarp(warpName);
                    if (warp != null) {
                        targetTeam = alliedTeam;
                        break;
                    }
                }
            }
        }

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
        TeleportUtil.delayedTeleport(player, warp, "warp", warpName);
        return true;
    }

    @Override
    public String getArgumentUsage() { return "<name>"; }

    @Override
    public String getDescription() { return "Teleport to a named team warp (from your team or allied teams)."; }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return java.util.Collections.emptyList();
        }
        
        Player player = (Player) sender;
        Team playerTeam = TeamManager.getInstance().getPlayerTeam(player);
        if (playerTeam == null) {
            return java.util.Collections.emptyList();
        }
        
        if (args.length == 1) {
            List<String> availableWarps = new ArrayList<>();
            // Add warps from player's own team
            availableWarps.addAll(playerTeam.getWarps().keySet());

            // Add warps from allied teams
            Set<String> teamsAlliedWithPlayerTeam = AllianceManager.getInstance().getTeamsAlliedWith(playerTeam.getName());
            for (String alliedTeamName : teamsAlliedWithPlayerTeam) {
                Team alliedTeam = TeamManager.getInstance().getTeam(alliedTeamName);
                if (alliedTeam != null) {
                    availableWarps.addAll(alliedTeam.getWarps().keySet());
                }
            }
            return availableWarps;
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }
}