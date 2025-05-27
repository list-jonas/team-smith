package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class SubCommandExecutor {
    public abstract boolean execute(Player player, String[] args, TeamManager teamManager);
    public abstract String getDescription(); // e.g., "Creates a new team."
    public abstract PermissionLevel getRequiredPermissionLevel();

    /**
     * Returns tab completions for the given arguments and sender.
     * Default implementation returns an empty list.
     */
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Returns the argument usage for this command.
     * This should be a string that describes how to use the command, e.g., "<teamName>" or "<playerName> <role>".
     * Default implementation returns an empty string.
     */
    public String getArgumentUsage() {
        return "";
    };
}