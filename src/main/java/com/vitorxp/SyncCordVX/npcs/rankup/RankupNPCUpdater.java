package com.vitorxp.SyncCordVX.npcs.rankup;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.scheduler.BukkitRunnable;

public class RankupNPCUpdater extends BukkitRunnable {

    private final SyncCordVX plugin;

    public RankupNPCUpdater(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Verifica se o manager existe antes de chamar
        if (plugin.getRankupNpcManager() != null) {
            plugin.getRankupNpcManager().updateRankupNpcs();
        }
    }
}