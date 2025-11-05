package com.vitorxp.SyncCordVX.listeners;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private static Plugin plugin;

    public static void init(Plugin p) {
        plugin = p;
    }

    public static void logToFile(String fileName, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    File dataFolder = plugin.getDataFolder();
                    if (!dataFolder.exists()) {
                        dataFolder.mkdir();
                    }

                    File logFile = new File(dataFolder, fileName);
                    if (!logFile.exists()) {
                        logFile.createNewFile();
                    }

                    try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        out.println("[" + timestamp + "] " + message);
                        out.println("---------------------------------");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}