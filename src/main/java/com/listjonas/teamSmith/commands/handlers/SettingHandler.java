package com.listjonas.teamSmith.commands.handlers;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.commands.TeamCommand;
import com.listjonas.teamSmith.manager.TeamManager;
import com.listjonas.teamSmith.model.PermissionLevel;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingHandler extends SubCommandExecutor {
    private final Map<String, SubCommandExecutor> subHandlers;
    private final TeamManager teamManager; // Added to access teamManager for permission checks

    public SettingHandler(TeamManager teamManager) {
        this.teamManager = teamManager;
        this.subHandlers = new HashMap<>();
        // These will be populated from TeamCommand or directly here
        subHandlers.put("transfer", new TransferOwnershipHandler());
        subHandlers.put("rename", new RenameTeamHandler());
        subHandlers.put("setprefix", new SetPrefixHandler());
        subHandlers.put("setprefixcolor", new SetPrefixColorHandler());
        subHandlers.put("setrole", new SetRoleHandler());
        subHandlers.put("setmotd", new SetMotdHandler());
        subHandlers.put("friendlyfire", new FriendlyFireHandler());
        subHandlers.put("ff", subHandlers.get("friendlyfire"));
        subHandlers.put("setideology", new SetIdeologyHandler());
        subHandlers.put("delete",new DeleteTeamHandler());
        subHandlers.put("disband",subHandlers.get("delete"));
    }

    @Override
    public boolean execute(Player player, String[] args, TeamManager teamManager) {
        if (args.length == 0) {
            sendSettingHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        SubCommandExecutor handler = subHandlers.get(subCommand);

        if (handler == null) {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "Unknown setting command: " + subCommand);
            sendSettingHelpMessage(player);
            return true;
        }

        // Permission check for the specific sub-handler
        Team playerTeam = teamManager.getPlayerTeam(player);
        int playerPermissionLevel = -1;
        if (playerTeam != null) {
            Team.Role playerRole = playerTeam.getPlayerRole(player.getUniqueId());
            if (playerRole != null) {
                playerPermissionLevel = playerRole.getPermissionLevel();
            }
        }

        if (playerPermissionLevel >= handler.getRequiredPermissionLevel().getLevel()) {
            return handler.execute(player, Arrays.copyOfRange(args, 1, args.length), teamManager);
        } else {
            player.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ERROR_COLOR + "You don't have permission to use this setting command.");
            return true;
        }
    }

    private void sendSettingHelpMessage(Player p) {
        p.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "--- Team Settings Help ---");
        Team playerTeam = teamManager.getPlayerTeam(p); // Use the passed teamManager instance
        int playerPermissionLevel = -1;
        if (playerTeam != null) {
            Team.Role playerRole = playerTeam.getPlayerRole(p.getUniqueId());
            if (playerRole != null) {
                playerPermissionLevel = playerRole.getPermissionLevel();
            }
        }

        final int finalPlayerPermissionLevel = playerPermissionLevel;
        subHandlers.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("ff")) // Don't show alias in help
            .sorted(Map.Entry.comparingByKey()) // Sort alphabetically for consistent help output
            .forEach((entry) -> {
                String k = entry.getKey();
                SubCommandExecutor h = entry.getValue();
                if (finalPlayerPermissionLevel >= h.getRequiredPermissionLevel().getLevel()) {
                    p.sendMessage(TeamCommand.INFO_COLOR + "/team setting " + k + (h.getArgumentUsage().isEmpty() ? "" : " " + h.getArgumentUsage()) + ChatColor.GRAY + " - " + h.getDescription());
                }
        });
        p.sendMessage(TeamCommand.MSG_PREFIX + TeamCommand.ACCENT_COLOR + "------------------------");
    }

    @Override
    public String getDescription() {
        return "Manages various team settings.";
    }

    @Override
    public PermissionLevel getRequiredPermissionLevel() {
        // The base 'setting' command can be accessible by members,
        // individual sub-settings will have their own stricter permissions.
        return PermissionLevel.MEMBER;
    }

    @Override
    public String getArgumentUsage() {
        return "<setting_subcommand> [args]";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        Team playerTeam = teamManager.getPlayerTeam(player);
        int playerPermissionLevel = -1;
        if (playerTeam != null) {
            Team.Role playerRole = playerTeam.getPlayerRole(player.getUniqueId());
            if (playerRole != null) {
                playerPermissionLevel = playerRole.getPermissionLevel();
            }
        }

        if (args.length == 1) {
            final int finalPlayerPermissionLevel = playerPermissionLevel;
            return subHandlers.entrySet().stream()
                    .filter(entry -> finalPlayerPermissionLevel >= entry.getValue().getRequiredPermissionLevel().getLevel())
                    .map(Map.Entry::getKey)
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        SubCommandExecutor handler = subHandlers.get(args[0].toLowerCase());
        if (handler == null || playerPermissionLevel < handler.getRequiredPermissionLevel().getLevel()) {
            return Collections.emptyList();
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        List<String> suggestions = handler.getTabCompletions(sender, subArgs);
        String current = subArgs.length > 0 ? subArgs[subArgs.length - 1].toLowerCase() : "";
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}