package com.vitorxp.SyncCordVX.utils;

import org.bukkit.scheduler.BukkitRunnable;

public class TPSMonitor extends BukkitRunnable {

    private static double tps = 20.0;
    private long lastTick = System.currentTimeMillis();
    private int ticks = 0;

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        // Calcula a diferença de tempo desde a última verificação
        long elapsed = now - lastTick;
        lastTick = now;

        // Calcula o TPS baseado no tempo decorrido e na quantidade de ticks
        double currentTps = (double) ticks * 1000.0 / elapsed;

        // Limita o TPS a um máximo de 20.0
        if (currentTps > 20.0) {
            currentTps = 20.0;
        }

        tps = currentTps;
        ticks = 0; // Reseta a contagem de ticks para o próximo segundo
    }

    // Este método será chamado a cada tick pelo servidor
    public static void tick() {
        getInstance().ticks++;
    }

    public static double getTPS() {
        return tps;
    }

    // Singleton para garantir que só tenhamos um monitor
    private static TPSMonitor instance;
    public static TPSMonitor getInstance() {
        if (instance == null) {
            instance = new TPSMonitor();
        }
        return instance;
    }
}