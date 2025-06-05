package com.listjonas.teamSmith.manager;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

public class TablistManager {

    private final TeamSmith plugin;
    private final TeamManager teamManager;
    private final DecimalFormat tpsFormat = new DecimalFormat("##.00");
    private final TextColor GRADIENT_ORANGE = TextColor.color(255, 170, 0);
    private final TextColor GRADIENT_YELLOW = TextColor.color(255, 255, 85);
    private final int GRADIENT_LENGTH = 45;
    private final String GRADIENT_CHAR = "━"; // Using U+25AC BLACK RECTANGLE ━

    public TablistManager(TeamSmith plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    private Component generateGradientBar(TextColor startColor, TextColor endColor, int length, String character) {
        Component bar = Component.empty();
        if (length <= 0) return bar;
        for (int i = 0; i < length; i++) {
            float ratio = (length == 1) ? 0f : (float) i / (length - 1);
            int r = (int) (startColor.red() + (endColor.red() - startColor.red()) * ratio);
            int g = (int) (startColor.green() + (endColor.green() - startColor.green()) * ratio);
            int b = (int) (startColor.blue() + (endColor.blue() - startColor.blue()) * ratio);
            bar = bar.append(Component.text(character, TextColor.color(r, g, b)));
        }
        return bar;
    }

    public void updatePlayerTabName(Player player) {
        Team team = teamManager.getPlayerTeam(player);
        String prefix = "";
        String teamName = "";
        NamedTextColor teamColor = NamedTextColor.WHITE; // Default color

        if (team != null) {
            prefix = team.getPrefix() != null ? team.getPrefix() : "";
            teamName = team.getName();
            try {
                if (team.getPrefixColor() != null && !team.getPrefixColor().isEmpty()) {
                    // Bukkit ChatColor to Adventure NamedTextColor
                    org.bukkit.ChatColor bukkitColor = ChatColor.getByChar(team.getPrefixColor().replace("&", "").charAt(0));
                    if (bukkitColor != null) {
                        teamColor = NamedTextColor.namedColor(bukkitColor.asBungee().getColor().getRGB() & 0xFFFFFF);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse team prefix color: " + team.getPrefixColor() + " for team " + teamName);
            }
            if (!prefix.isEmpty()) {
                player.displayName(Component.text(player.getName(), NamedTextColor.WHITE));
                player.playerListName(Component.text(prefix + " ", teamColor).append(Component.text(player.getName(), teamColor)));
            } else {
                player.displayName(Component.text(player.getName(), NamedTextColor.WHITE));
                player.playerListName(Component.text(player.getName(), NamedTextColor.WHITE));
            }
        } else {
            player.displayName(Component.text(player.getName(), NamedTextColor.WHITE));
            player.playerListName(Component.text(player.getName(), NamedTextColor.WHITE));
        }
    }

    public void updateTabListHeaderForAllPlayers() {
        Component header = generateGradientBar(GRADIENT_ORANGE, GRADIENT_YELLOW, GRADIENT_LENGTH, GRADIENT_CHAR);
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.sendPlayerListHeader(header);
        }
    }

    public void updateTabListFooterForAllPlayers() {
        double tps = Bukkit.getTPS()[0]; // Get the 1-minute average TPS
        String tpsString = tpsFormat.format(tps);

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;

        Component gradientBar = generateGradientBar(GRADIENT_ORANGE, GRADIENT_YELLOW, GRADIENT_LENGTH, GRADIENT_CHAR);

        Component footerInfo = Component.text("TPS: ", NamedTextColor.GREEN)
                                .append(Component.text(tpsString, NamedTextColor.GRAY))
                                .append(Component.newline())
                                .append(Component.text("RAM: ", NamedTextColor.AQUA))
                                .append(Component.text(usedMemory + "MB/" + maxMemory + "MB", NamedTextColor.GRAY));

        Component footer = gradientBar.append(Component.newline()).append(footerInfo);

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.sendPlayerListFooter(footer);
        }
    }

    public void updateAllPlayersTabInfo() {
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            updatePlayerTabName(onlinePlayer);
        }
        updateTabListHeaderForAllPlayers(); // Header might be static or updated less frequently
        updateTabListFooterForAllPlayers(); // Footer will update with TPS/CPU
    }

    // Schedule a repeating task to update the tab list footer periodically
    public void startTablistUpdater() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAllPlayersTabInfo, 0L, 20L * 5); // Update every 5 seconds
    }
}