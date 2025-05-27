package com.listjonas.teamSmith.model;

public enum PermissionLevel {
    PUBLIC(-1),
    MEMBER(0),
    MANAGER(1),
    OWNER(2);

    private final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}