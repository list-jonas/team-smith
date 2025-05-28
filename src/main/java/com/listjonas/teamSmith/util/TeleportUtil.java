package com.listjonas.teamSmith.util;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.commands.TeamCommand;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportUtil {

    public static void delayedTeleport(Player player, Location targetLocation, String teleportType, String name) {
        int delaySeconds = TeamSmith.getInstance().getConfigData().getTeleportDelaySeconds();

        if (delaySeconds > 0) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "Teleporting to " + teleportType + " '" + name + "' in " + delaySeconds + " seconds...");

            new BukkitRunnable() {
                int countdown = delaySeconds;

                @Override
                public void run() {
                    if (countdown <= 0) {
                        player.teleport(targetLocation);
                        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "You have been teleported to " + teleportType + " '" + name + "'.");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                        this.cancel();
                    } else {
                        player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.INFO_COLOR + "" + countdown + "...");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
                        countdown--;
                    }
                }
            }.runTaskTimer(TeamSmith.getInstance(), 0L, 20L); // 0L initial delay, 20L (1 second) repeat rate
        } else {
            player.teleport(targetLocation);
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.SUCCESS_COLOR + "You have been teleported to " + teleportType + " '" + name + "'.");
        }
    }
}