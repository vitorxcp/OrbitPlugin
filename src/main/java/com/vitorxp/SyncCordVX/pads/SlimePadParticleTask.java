package com.vitorxp.SyncCordVX.pads;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class SlimePadParticleTask extends BukkitRunnable {

    private final SyncCordVX plugin;

    public SlimePadParticleTask(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        SlimePadManager manager = plugin.getSlimePadManager();
        if (manager == null || manager.getSlimePads().isEmpty()) {
            return;
        }

        for (Location padLocation : manager.getSlimePads()) {
            World world = padLocation.getWorld();
            if (world != null) {
                Location particleLocation = padLocation.clone().add(0.5, 1.1, 0.5);

                world.playEffect(particleLocation, Effect.HAPPY_VILLAGER, 1);
            }
        }
    }
}