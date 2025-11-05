package com.vitorxp.SyncCordVX.utils;

import org.bukkit.scheduler.BukkitRunnable;

public class TPSMonitor extends BukkitRunnable {

    private static double tps = 20.0;
    private long lastTick = System.currentTimeMillis();
    private int ticks = 0;

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTick;
        lastTick = now;

        double currentTps = (double) ticks * 1000.0 / elapsed;

        if (currentTps > 20.0) {
            currentTps = 20.0;
        }

        tps = currentTps;
        ticks = 0;
    }

    public static void tick() {
        getInstance().ticks++;
    }

    public static double getTPS() {
        return tps;
    }

    private static TPSMonitor instance;
    public static TPSMonitor getInstance() {
        if (instance == null) {
            instance = new TPSMonitor();
        }
        return instance;
    }
}