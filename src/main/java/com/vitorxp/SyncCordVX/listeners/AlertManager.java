package com.vitorxp.SyncCordVX.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertManager {

    public static final String PERMISSION_NODE = "sync.alertas.ver";

    public static void alertStaff(String message) {
        // Bukkit.broadcast(message, PERMISSION_NODE);

        Bukkit.getConsoleSender().sendMessage(message);
    }
}