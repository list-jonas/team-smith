package com.listjonas.teamSmith;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    private String name;
    private UUID leader;
    private Set<UUID> members;
    private String prefix;

    // Constructor for creating a new team
    public Team(String name, Player leaderPlayer) {
        this.name = name;
        this.leader = leaderPlayer.getUniqueId();
        this.members = new HashSet<>();
        this.members.add(leaderPlayer.getUniqueId());
        this.prefix = "[" + name + "] "; // Default prefix
    }

    // Constructor for loading a team from data
    @SuppressWarnings("unchecked")
    public Team(String name, Map<String, Object> data) {
        this.name = name;
        this.leader = UUID.fromString((String) data.get("leader"));
        this.prefix = (String) data.get("prefix");
        List<String> memberUuids = (List<String>) data.get("members");
        this.members = memberUuids.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("leader", leader.toString());
        data.put("prefix", prefix);
        data.put("members", members.stream().map(UUID::toString).collect(Collectors.toList()));
        return data;
    }


    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean addMember(Player player) {
        return members.add(player.getUniqueId());
    }

    public boolean removeMember(Player player) {
        return members.remove(player.getUniqueId());
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUniqueId());
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getSize() {
        return members.size();
    }
}
