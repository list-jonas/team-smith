package com.listjonas.teamSmith;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamSmith plugin;
    private final TeamManager teamManager;

    // Define some standard colors for messages, aligning with TeamManager
    public static final String MSG_PREFIX = ChatColor.GOLD + "[TeamSmith] " + ChatColor.RESET;
    public static final String SUCCESS_COLOR = ChatColor.GREEN.toString();
    public static final String ERROR_COLOR = ChatColor.RED.toString();
    public static final String INFO_COLOR = ChatColor.YELLOW.toString();
    public static final String ACCENT_COLOR = ChatColor.AQUA.toString();

    public TeamCommand(TeamSmith plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MSG_PREFIX + ERROR_COLOR + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team create <teamName>");
                    return true;
                }
                teamManager.createTeam(args[1], player);
                break;
            case "delete":
            case "disband":
                Team playerTeamForDelete = teamManager.getPlayerTeam(player);
                if (playerTeamForDelete == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to delete/disband.");
                    return true;
                }
                teamManager.deleteTeam(playerTeamForDelete.getName(), player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team invite <playerName>");
                    return true;
                }
                Player targetInvite = Bukkit.getPlayer(args[1]);
                if (targetInvite == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Player '" + ACCENT_COLOR + args[1] + ERROR_COLOR + "' not found.");
                    return true;
                }
                Team teamToInvite = teamManager.getPlayerTeam(player);
                if (teamToInvite == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to invite players to.");
                    return true;
                }
                // For simplicity, directly adding. Consider an actual invite/accept system.
                teamManager.addPlayerToTeam(teamToInvite.getName(), targetInvite, player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team kick <playerName>");
                    return true;
                }
                Player targetKick = Bukkit.getPlayer(args[1]);
                if (targetKick == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Player '" + ACCENT_COLOR + args[1] + ERROR_COLOR + "' not found.");
                    return true;
                }
                Team teamToKickFrom = teamManager.getPlayerTeam(player);
                if (teamToKickFrom == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to kick players from.");
                    return true;
                }
                teamManager.removePlayerFromTeam(teamToKickFrom.getName(), targetKick, player);
                break;
            case "leave":
                Team teamToLeave = teamManager.getPlayerTeam(player);
                if (teamToLeave == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to leave.");
                    return true;
                }
                teamManager.removePlayerFromTeam(teamToLeave.getName(), player, player);
                break;
            case "transfer":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team transfer <newLeaderName>");
                    return true;
                }
                Player newLeader = Bukkit.getPlayer(args[1]);
                if (newLeader == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Player '" + ACCENT_COLOR + args[1] + ERROR_COLOR + "' not found.");
                    return true;
                }
                Team teamToTransfer = teamManager.getPlayerTeam(player);
                if (teamToTransfer == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to transfer leadership.");
                    return true;
                }
                teamManager.transferLeadership(teamToTransfer.getName(), newLeader, player);
                break;
            case "prefix":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team prefix <newPrefix>");
                    return true;
                }
                Team teamForPrefix = teamManager.getPlayerTeam(player);
                if (teamForPrefix == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to set a prefix for.");
                    return true;
                }
                // Combine remaining args for prefix with spaces
                String prefix = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                teamManager.setTeamPrefix(teamForPrefix.getName(), prefix, player);
                break;
            case "prefixcolor":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team prefixcolor <colorcode>");
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Example: /team prefixcolor &c (for red)");
                    return true;
                }
                Team teamForPrefixColor = teamManager.getPlayerTeam(player);
                if (teamForPrefixColor == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to set a prefix color for.");
                    return true;
                }
                String colorCode = args[1];
                teamManager.setTeamPrefixColor(teamForPrefixColor.getName(), colorCode, player);
                break;
            case "info":
                Team currentTeam = teamManager.getPlayerTeam(player);
                if (currentTeam == null) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "You are not currently in a team.");
                    return true;
                }
                sendTeamInfo(player, currentTeam);
                break;
            case "setrole":
                if (args.length < 3) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team setrole <playerName> <role>");
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Available roles: MANAGER, MEMBER");
                    return true;
                }
                Team teamForSetRole = teamManager.getPlayerTeam(player);
                if (teamForSetRole == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team.");
                    return true;
                }
                Player targetPlayerSetRole = Bukkit.getPlayer(args[1]);
                if (targetPlayerSetRole == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Player '" + ACCENT_COLOR + args[1] + ERROR_COLOR + "' not found.");
                    return true;
                }
                Team.Role newRole;
                try {
                    newRole = Team.Role.valueOf(args[2].toUpperCase());
                    if (newRole == Team.Role.OWNER) {
                        player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Cannot set role to OWNER using this command. Use /team transfer.");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "Invalid role '" + ACCENT_COLOR + args[2] + ERROR_COLOR + "'. Available roles: MANAGER, MEMBER.");
                    return true;
                }
                teamManager.promotePlayerRole(teamForSetRole.getName(), targetPlayerSetRole, newRole, player);
                break;
            case "motd":
                if (args.length < 2) {
                    player.sendMessage(MSG_PREFIX + INFO_COLOR + "Usage: /team motd <message>");
                    return true;
                }
                Team teamForMotd = teamManager.getPlayerTeam(player);
                if (teamForMotd == null) {
                    player.sendMessage(MSG_PREFIX + ERROR_COLOR + "You are not in a team to set an MOTD for.");
                    return true;
                }
                String motd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                teamManager.setTeamMotd(teamForMotd.getName(), motd, player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(MSG_PREFIX + ACCENT_COLOR + "--- TeamSmith Help ---");
        player.sendMessage(INFO_COLOR + "/team create <teamName>" + ChatColor.GRAY + " - Creates a new team.");
        player.sendMessage(INFO_COLOR + "/team delete" + ChatColor.GRAY + " - Deletes your current team (OWNER only).");
        player.sendMessage(INFO_COLOR + "/team invite <playerName>" + ChatColor.GRAY + " - Invites a player to your team.");
        player.sendMessage(INFO_COLOR + "/team kick <playerName>" + ChatColor.GRAY + " - Kicks a player from your team (OWNER/MANAGER only).");
        player.sendMessage(INFO_COLOR + "/team leave" + ChatColor.GRAY + " - Leaves your current team.");
        player.sendMessage(INFO_COLOR + "/team transfer <newLeaderName>" + ChatColor.GRAY + " - Transfers ownership (OWNER only).");
        player.sendMessage(INFO_COLOR + "/team setrole <playerName> <role>" + ChatColor.GRAY + " - Sets a player's role (OWNER only). Roles: MANAGER, MEMBER.");
        player.sendMessage(INFO_COLOR + "/team prefix <newPrefix>" + ChatColor.GRAY + " - Sets your team's chat prefix (OWNER/MANAGER only).");
        player.sendMessage(INFO_COLOR + "/team prefixcolor <colorcode>" + ChatColor.GRAY + " - Sets your team's prefix color (OWNER/MANAGER only).");
        player.sendMessage(INFO_COLOR + "/team friendlyfire <on|off>" + ChatColor.GRAY + " - Toggles friendly fire for your team. (Aliases: /team ff)");
        player.sendMessage(INFO_COLOR + "/team info" + ChatColor.GRAY + " - Shows information about your current team.");
        player.sendMessage(MSG_PREFIX + ACCENT_COLOR + "--------------------");
    }

    private void sendTeamInfo(Player player, Team team) {
        player.sendMessage(MSG_PREFIX + ACCENT_COLOR + "--- Team: " + team.getName() + " ---");
        UUID ownerId = team.getOwner();
        Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
        player.sendMessage(INFO_COLOR + "Owner: " + ACCENT_COLOR + (owner != null ? owner.getName() : (ownerId != null ? "Offline UUID: " + ownerId.toString().substring(0,8) : "Unknown")));
        
        String prefixText = team.getPrefix() == null || team.getPrefix().isEmpty() ? "Not set" : team.getPrefix();
        String prefixColor = team.getPrefixColor();
        String displayPrefix = ChatColor.translateAlternateColorCodes('&', prefixColor + prefixText) + ChatColor.RESET;
        player.sendMessage(INFO_COLOR + "Prefix: " + displayPrefix + (prefixText.equals("Not set") ? "" : INFO_COLOR + " (Raw: " + ACCENT_COLOR + prefixColor + prefixText + INFO_COLOR + ")"));
        player.sendMessage(INFO_COLOR + "Members (" + ACCENT_COLOR + team.getSize() + INFO_COLOR + "):");

        Map<UUID, Team.Role> memberRoles = team.getMemberRoles();
        List<UUID> sortedMembers = new ArrayList<>(memberRoles.keySet());
        // Sort members to show Owner first, then Managers, then Members
        sortedMembers.sort(Comparator.comparing((UUID id) -> memberRoles.get(id)).reversed());

        for (UUID memberId : sortedMembers) {
            Player member = Bukkit.getPlayer(memberId);
            Team.Role role = memberRoles.get(memberId);
            String roleString = ACCENT_COLOR + "(" + role.name() + ")";
            player.sendMessage(ChatColor.GRAY + "- " + ACCENT_COLOR + (member != null ? member.getName() : "Offline UUID: " + memberId.toString().substring(0,8)) + " " + roleString);
        }
        player.sendMessage(MSG_PREFIX + ACCENT_COLOR + "--------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> subCommands = Arrays.asList("create", "delete", "disband", "invite", "kick", "leave", "transfer", "prefix", "prefixcolor", "info", "setrole");

        if (args.length == 1) {
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            // Player name suggestions for relevant commands
            if (Arrays.asList("invite", "kick", "transfer", "setrole").contains(subCommand)) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            }
            // Color code suggestions for prefixcolor
            if (subCommand.equals("prefixcolor")) {
                List<String> colorCodes = Arrays.asList("&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o", "&r");
                for (String color : colorCodes) {
                    if (color.startsWith(args[1].toLowerCase())) {
                        completions.add(color);
                    }
                }
            }
            // Tab completion for friendlyfire on/off
            if (subCommand.equals("friendlyfire") || subCommand.equals("ff")) {
                if ("on".startsWith(args[1].toLowerCase())) {
                    completions.add("on");
                }
                if ("off".startsWith(args[1].toLowerCase())) {
                    completions.add("off");
                }
            }
            // No specific tab completion for motd message, as it's free text
            if (subCommand.equals("motd")) {
                // No completions needed for free text
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setrole")) {
            // Role suggestions for setrole command
            List<String> roles = Arrays.stream(Team.Role.values())
                                       .filter(role -> role != Team.Role.OWNER) // Don't suggest OWNER for setrole
                                       .map(Enum::name)
                                       .collect(Collectors.toList());
            for (String role : roles) {
                if (role.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(role);
                }
            }
        }

        return completions;
    }
}