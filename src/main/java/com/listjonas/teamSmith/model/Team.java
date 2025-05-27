package com.listjonas.teamSmith.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import java.util.*;

import org.bukkit.Location;
import com.listjonas.teamSmith.util.LocationUtil;

public class Team {
    private String name;
    private Map<UUID, Role> memberRoles;
    private String prefix;
    private String prefixColor; // Added for prefix color
    private boolean friendlyFireEnabled; // Added for friendly fire setting
    private String teamMotd; // Added for team MOTD
    private final Multimap<UUID,String> pendingInvites = ArrayListMultimap.create();


    public enum Role {
        OWNER,
        MANAGER,
        MEMBER
    }

    private Location homeLocation;
    private Map<String, Location> warps = new HashMap<>(); // up to 3 named warps

    // Constructor for creating a new team
    public Team(String name, Player leaderPlayer) {
        this.name = name;
        this.memberRoles = new HashMap<>();
        this.memberRoles.put(leaderPlayer.getUniqueId(), Role.OWNER);
        this.prefix = "[" + name + "] "; // Default prefix
        this.prefixColor = "&f"; // Default to white color
        this.friendlyFireEnabled = true; // Default to true (friendly fire enabled)
        this.teamMotd = ""; // Default to empty MOTD
        this.homeLocation = null;
        this.warps = new HashMap<>();
    }

    // Constructor for loading a team from data
    @SuppressWarnings("unchecked")
    public Team(String name, Map<String, Object> data) {
        this.name = name;
        this.prefix = (String) data.get("prefix");
        this.prefixColor = (String) data.getOrDefault("prefixColor", "&f"); // Default to white if not found
        this.friendlyFireEnabled = (boolean) data.getOrDefault("friendlyFireEnabled", true); // Default to true if not found
        this.teamMotd = (String) data.getOrDefault("teamMotd", ""); // Default to empty if not found

        this.memberRoles = new HashMap<>();
        Object rolesObj = data.get("memberRoles");
        Map<String, String> rolesData = null;
        if (rolesObj instanceof MemorySection) {
            rolesData = new HashMap<>();
            MemorySection section = (MemorySection) rolesObj;
            for (String key : section.getKeys(false)) {
                rolesData.put(key, section.getString(key));
            }
        } else if (rolesObj instanceof Map) {
            rolesData = (Map<String, String>) rolesObj;
        }
        if (rolesData != null) {
            for (Map.Entry<String, String> entry : rolesData.entrySet()) {
                try {
                    this.memberRoles.put(UUID.fromString(entry.getKey()), Role.valueOf(entry.getValue()));
                } catch (IllegalArgumentException e) {
                    // Handle cases where role string is invalid or UUID is malformed
                    System.err.println("Error loading role for team " + name + ": Invalid role or UUID string " + entry.getValue() + " / " + entry.getKey());
                }
            }
        }

        // === deserialize homeLocation if present ===
        Object homeObj = data.get("homeLocation");
        if (homeObj instanceof MemorySection) {
            MemorySection homeSection = (MemorySection) homeObj;
            this.homeLocation = LocationUtil.deserializeLocation(
                    homeSection.getValues(false)
            );
        } else if (homeObj instanceof Map) {
            // in case DataManager flattened it to a raw Map
            this.homeLocation = LocationUtil.deserializeLocation(
                    (Map<String, Object>) homeObj
            );
        }

        // === deserialize named warps if present ===
        Object warpsObj = data.get("warps");
        if (warpsObj instanceof MemorySection) {
            MemorySection warpsSection = (MemorySection) warpsObj;
            for (String warpKey : warpsSection.getKeys(false)) {
                Map<String, Object> warpData = warpsSection.getConfigurationSection(warpKey).getValues(false);
                Location loc = LocationUtil.deserializeLocation(warpData);
                if (loc != null) {
                    this.warps.put(warpKey, loc);
                }
            }
        } else if (warpsObj instanceof Map) {
            Map<String, Map<String, Object>> warpsData = (Map<String, Map<String, Object>>) warpsObj;
            for (Map.Entry<String, Map<String, Object>> e : warpsData.entrySet()) {
                Location loc = LocationUtil.deserializeLocation(e.getValue());
                if (loc != null) {
                    this.warps.put(e.getKey(), loc);
                }
            }
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("prefix", prefix);
        data.put("prefixColor", prefixColor);
        data.put("friendlyFireEnabled", friendlyFireEnabled);
        data.put("teamMotd", teamMotd);
        Map<String, String> serializedRoles = new HashMap<>();
        for (Map.Entry<UUID, Role> entry : memberRoles.entrySet()) {
            serializedRoles.put(entry.getKey().toString(), entry.getValue().name());
        }
        data.put("memberRoles", serializedRoles);
        // Serialize homeLocation if present
        if (homeLocation != null) {
            data.put("homeLocation", LocationUtil.serializeLocation(homeLocation));
        }
        // Serialize warps if present
        if (warps != null && !warps.isEmpty()) {
            Map<String, Map<String, Object>> serializedWarps = new HashMap<>();
            for (Map.Entry<String, org.bukkit.Location> entry : warps.entrySet()) {
                serializedWarps.put(entry.getKey(), LocationUtil.serializeLocation(entry.getValue()));
            }
            data.put("warps", serializedWarps);
        }
        return data;
    }

    public void broadcastMessage(String s) {
        for (UUID memberId : memberRoles.keySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                player.sendMessage(prefixColor + prefix + s);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String newTeamName) {
        if (newTeamName == null || newTeamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        this.name = newTeamName;
    }

    public UUID getOwner() {
        for (Map.Entry<UUID, Role> entry : memberRoles.entrySet()) {
            if (entry.getValue() == Role.OWNER) {
                return entry.getKey();
            }
        }
        return null; // Should not happen in a valid team
    }

    public void setOwner(UUID newOwnerUuid) {
        UUID oldOwnerUuid = getOwner();
        if (oldOwnerUuid != null) {
            memberRoles.put(oldOwnerUuid, Role.MEMBER); // Demote old owner to member or another role
        }
        memberRoles.put(newOwnerUuid, Role.OWNER);
        if (!memberRoles.containsKey(newOwnerUuid)) { // If new owner wasn't a member, add them
            // This case should ideally be handled by ensuring the new owner is already a member
        }
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(memberRoles.keySet());
    }

    public Map<UUID, Role> getMemberRoles() {
        return Collections.unmodifiableMap(memberRoles);
    }

    public boolean addMember(Player player) {
        return memberRoles.putIfAbsent(player.getUniqueId(), Role.MEMBER) == null;
    }

    public boolean removeMember(Player player) {
        return memberRoles.remove(player.getUniqueId()) != null;
    }

    public boolean isMember(Player player) {
        return memberRoles.containsKey(player.getUniqueId());
    }

    public boolean isLeader(Player player) { // Kept for compatibility, now checks for OWNER role
        return getPlayerRole(player.getUniqueId()) == Role.OWNER;
    }

    public Role getPlayerRole(UUID playerUuid) {
        return memberRoles.get(playerUuid);
    }

    public void setPlayerRole(UUID playerUuid, Role role) {
        if (memberRoles.containsKey(playerUuid)) {
            // If changing an owner, ensure there's a proper transfer of ownership handled elsewhere (e.g., TeamManager)
            if (role == Role.OWNER) {
                UUID currentOwner = getOwner();
                if (currentOwner != null && !currentOwner.equals(playerUuid)) {
                    memberRoles.put(currentOwner, Role.MEMBER); // Demote current owner
                }
            }
            memberRoles.put(playerUuid, role);
        } else {
            // Player not in team, cannot set role. Or handle as an error/exception.
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefixColor() {
        return prefixColor;
    }

    public void setPrefixColor(String prefixColor) {
        this.prefixColor = prefixColor;
    }

    public int getSize() {
        return memberRoles.size();
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFireEnabled;
    }

    public void setFriendlyFireEnabled(boolean friendlyFireEnabled) {
        this.friendlyFireEnabled = friendlyFireEnabled;
    }

    public String getTeamMotd() {
        return teamMotd;
    }

    public void setTeamMotd(String teamMotd) {
        this.teamMotd = teamMotd;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void deleteHomeLocation() {
        this.homeLocation = null;
    }

    public Map<String, Location> getWarps() {
        return Collections.unmodifiableMap(warps);
    }

    public boolean setWarp(String name, Location location) {
        if (warps.size() >= 3 && !warps.containsKey(name)) return false;
        warps.put(name, location);
        return true;
    }

    public boolean deleteWarp(String name) {
        return warps.remove(name) != null;
    }

    public Location getWarp(String name) {
        return warps.get(name);
    }

    public void invitePlayer(Player invited, String teamName) {
        pendingInvites.put(invited.getUniqueId(), teamName);
    }

    public boolean hasInvite(UUID playerId, String teamName) {
        return pendingInvites.containsEntry(playerId, teamName);
    }

    public void removeInvite(UUID playerId, String teamName) {
        pendingInvites.remove(playerId, teamName);
    }
}
