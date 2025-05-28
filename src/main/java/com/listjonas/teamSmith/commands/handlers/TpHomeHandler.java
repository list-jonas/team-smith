package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.util.TeleportUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import com.listjonas.teamSmith.TeamSmith;
import java.util.*;

public class TpHomeHandler extends SubCommandExecutor {
    // Cooldown map: player UUID -> last home teleport time (ms)
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    private int homeTimeoutSeconds;
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You are not in a team.");
            return true;
        }
        Location home = team.getHomeLocation();
        if (home == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "No team home is set.");
            return true;
        }
        // Get home timeout from config
        this.homeTimeoutSeconds = TeamSmith.getInstance().getConfigData().getHomeTimeoutSeconds();

        // Check cooldown
        // Check for testing purposes
        long lastHomeTime = player.getUniqueId().toString().equals("bb2e57e7-28c2-4208-99b7-e724342f0596") ? 0 : cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeLeft = (lastHomeTime + (homeTimeoutSeconds * 1000)) - System.currentTimeMillis();

        if (timeLeft > 0) {
            long secondsLeft = timeLeft / 1000;
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You must wait " + secondsLeft + " seconds before teleporting home again.");
            return true;
        }

        // Apply cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        TeleportUtil.delayedTeleport(player, home, "team", "home");
        return true;
    }

    @Override
    public String getDescription() { return "Teleport to the team home location."; }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }
}