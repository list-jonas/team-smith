package com.listjonas.teamSmith;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    private String name;
    private Map<UUID, Role> memberRoles;
    private String prefix;
    private String prefixColor; // Added for prefix color

    public enum Role {
        OWNER,
        MANAGER,
        MEMBER
    }

    // Constructor for creating a new team
    public Team(String name, Player leaderPlayer) {
        this.name = name;
        this.memberRoles = new HashMap<>();
        this.memberRoles.put(leaderPlayer.getUniqueId(), Role.OWNER);
        this.prefix = "[" + name + "] "; // Default prefix
        this.prefixColor = "&f"; // Default to white color
    }

    // Constructor for loading a team from data
    @SuppressWarnings("unchecked")
    public Team(String name, Map<String, Object> data) {
        this.name = name;
        this.prefix = (String) data.get("prefix");
        this.prefixColor = (String) data.getOrDefault("prefixColor", "&f"); // Default to white if not found
        
        this.memberRoles = new HashMap<>();
        Map<String, String> rolesData = (Map<String, String>) data.get("memberRoles");
        if (rolesData != null) {
            for (Map.Entry<String, String> entry : rolesData.entrySet()) {
                try {
                    this.memberRoles.put(UUID.fromString(entry.getKey()), Role.valueOf(entry.getValue()));
                } catch (IllegalArgumentException e) {
                    // Handle cases where role string is invalid or UUID is malformed
                    System.err.println("Error loading role for team " + name + ": Invalid role or UUID string " + entry.getValue() + " / " + entry.getKey());
                    // Optionally, assign a default role or skip this member
                }
            }
        }

        // Backwards compatibility for old data structure (leader and members list)
        if (this.memberRoles.isEmpty() && data.containsKey("leader") && data.containsKey("members")) {
            UUID oldLeader = UUID.fromString((String) data.get("leader"));
            this.memberRoles.put(oldLeader, Role.OWNER);
            List<String> memberUuids = (List<String>) data.get("members");
            if (memberUuids != null) {
                memberUuids.stream()
                    .map(UUID::fromString)
                    .filter(uuid -> !uuid.equals(oldLeader)) // Ensure leader isn't added again as member
                    .forEach(uuid -> this.memberRoles.put(uuid, Role.MEMBER));
            }
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("prefix", prefix);
        data.put("prefixColor", prefixColor);
        Map<String, String> serializedRoles = new HashMap<>();
        for (Map.Entry<UUID, Role> entry : memberRoles.entrySet()) {
            serializedRoles.put(entry.getKey().toString(), entry.getValue().name());
        }
        data.put("memberRoles", serializedRoles);
        // data.put("leader", getOwner().toString()); // Store owner explicitly if needed, or derive from roles
        // data.put("members", members.stream().map(UUID::toString).collect(Collectors.toList())); // Old serialization
        return data;
    }


    public String getName() {
        return name;
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
}
