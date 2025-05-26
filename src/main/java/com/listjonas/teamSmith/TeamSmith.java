package com.listjonas.teamSmith;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TeamSmith extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(TeamSmith.class);

    @Override
    public void onEnable() {
        // Plugin startup logic
        log.info("TeamSmith plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info("TeamSmith plugin has been disabled!");
    }
}
