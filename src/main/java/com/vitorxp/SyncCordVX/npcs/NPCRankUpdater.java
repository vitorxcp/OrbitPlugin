package com.vitorxp.SyncCordVX.npcs;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCRankUpdater extends BukkitRunnable {

    private final SyncCordVX plugin;

    public NPCRankUpdater(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getNpcManager().updateRankedNpcs();
    }
}