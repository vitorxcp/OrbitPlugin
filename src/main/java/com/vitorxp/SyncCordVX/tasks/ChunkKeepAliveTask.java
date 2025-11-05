package com.vitorxp.SyncCordVX.tasks;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.chunks.ChunkManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkKeepAliveTask extends BukkitRunnable {

    private final SyncCordVX plugin;

    public ChunkKeepAliveTask(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ChunkManager manager = plugin.getChunkManager();
        if (manager == null || manager.getKeptChunks().isEmpty()) {
            return;
        }

        for (Location loc : manager.getKeptChunks()) {
            World world = loc.getWorld();
            if (world != null) {
                int chunkX = loc.getBlockX() >> 4;
                int chunkZ = loc.getBlockZ() >> 4;

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ);
                }
            }
        }
    }
}