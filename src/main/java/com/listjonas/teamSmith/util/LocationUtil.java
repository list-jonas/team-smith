package com.listjonas.teamSmith.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.HashMap;
import java.util.Map;

public class LocationUtil {
    public static Map<String, Object> serializeLocation(Location loc) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", loc.getWorld().getName());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", loc.getYaw());
        map.put("pitch", loc.getPitch());
        return map;
    }

    public static Location deserializeLocation(Map<String, Object> map) {
        if (map == null) return null;
        World world = Bukkit.getWorld((String) map.get("world"));
        if (world == null) return null;
        double x = (map.get("x") instanceof Number) ? ((Number) map.get("x")).doubleValue() : Double.parseDouble(map.get("x").toString());
        double y = (map.get("y") instanceof Number) ? ((Number) map.get("y")).doubleValue() : Double.parseDouble(map.get("y").toString());
        double z = (map.get("z") instanceof Number) ? ((Number) map.get("z")).doubleValue() : Double.parseDouble(map.get("z").toString());
        float yaw = (map.get("yaw") instanceof Number) ? ((Number) map.get("yaw")).floatValue() : Float.parseFloat(map.get("yaw").toString());
        float pitch = (map.get("pitch") instanceof Number) ? ((Number) map.get("pitch")).floatValue() : Float.parseFloat(map.get("pitch").toString());
        return new Location(world, x, y, z, yaw, pitch);
    }
}