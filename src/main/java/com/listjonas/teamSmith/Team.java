package com.listjonas.teamsmith;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    private String name;
    private UUID leader;
    private Set<UUID> members;
    private String prefix;

    public Team(String name, Player leader) {
        this.name = name;
        this.leader = leader.getUniqueId();
        this.members = new HashSet<>();
        this.members.add(leader.getUniqueId());
        this.prefix = "[" + name + "] "; // Default prefix
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