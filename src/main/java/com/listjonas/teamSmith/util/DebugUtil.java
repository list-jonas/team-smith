package com.listjonas.teamSmith.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DebugUtil {

    private static final Set<UUID> DEBUG_PLAYERS = new HashSet<>(Arrays.asList(
        UUID.fromString("bb2e57e7-28c2-4208-99b7-e724342f0596"),
        UUID.fromString("a1866f62-d401-4b61-8e5d-8575876a1698")
    ));

    public static boolean isDebugPlayer(UUID playerUuid) {
        return DEBUG_PLAYERS.contains(playerUuid);
    }
}