package com.vitorxp.SyncCordVX.tasks;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.listeners.AlertManager;
import com.vitorxp.SyncCordVX.listeners.LogManager;
import com.vitorxp.SyncCordVX.listeners.Profiler;
import org.bukkit.scheduler.BukkitRunnable;

public class TPSWatcher extends BukkitRunnable {

    private long lastTick = System.currentTimeMillis();
    private long lagDuration = 0;
    private boolean isLagging = false;
    private final double LAG_THRESHOLD = 18.0;

    private final SyncCordVX plugin;

    public TPSWatcher(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTick;
        this.lastTick = now;

        double tps = 20.0 * (1000.0 / Math.max(1000.0, elapsed));

        if (tps < LAG_THRESHOLD && !isLagging) {
            this.isLagging = true;
            this.lagDuration = System.currentTimeMillis();
            onLagDetected();
        } else if (tps >= LAG_THRESHOLD && isLagging) {
            this.isLagging = false;
            long totalLagTime = System.currentTimeMillis() - this.lagDuration;
            onLagRecovered(totalLagTime);
        }
    }

    private void onLagDetected() {
        String[] top3 = Profiler.getTop3Culprits(plugin);

        String report = String.format("§c[Alerta] Servidor começou a lagar! (TPS: %.2f)",  15.0);

        String details = "Top 3 prováveis culpados:\n1. " + top3[0] + "\n2. " + top3[1] + "\n3. " + top3[2];

        AlertManager.alertStaff(report + "\n" + details);

        LogManager.logToFile("lag_reports.txt", report + "\n" + details);
    }

    private void onLagRecovered(long durationMillis) {
        double durationSeconds = durationMillis / 1000.0;
        String report = String.format("§a[Alerta] Servidor recuperado. O lag durou %.2f segundos.", durationSeconds);

        AlertManager.alertStaff(report);
        LogManager.logToFile("lag_reports.txt", report);
    }
}