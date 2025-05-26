package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommandExecutor {
    boolean execute(Player player, String[] args, TeamManager teamManager);
    String getArgumentUsage(); // e.g., "<teamName>" or "<playerName> <role>"
    String getDescription(); // e.g., "Creates a new team."
    /**
     * Returns tab completions for the given arguments and sender.
     */
    List<String> getTabCompletions(CommandSender sender, String[] args);
}