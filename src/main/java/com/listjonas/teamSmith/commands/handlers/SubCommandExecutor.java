package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.entity.Player;

public interface SubCommandExecutor {
    boolean execute(Player player, String[] args, TeamManager teamManager);
    String getArgumentUsage(); // e.g., "<teamName>" or "<playerName> <role>"
    String getDescription(); // e.g., "Creates a new team."
}