package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.util.TeleportUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelTpHandler extends SubCommandExecutor {
    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        TeleportUtil.cancelTeleport(player);
        return true;
    }

    @Override
    public String getDescription() {
        return "Cancel a pending teleportation.";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        return PermissionLevel.MEMBER;
    }
}