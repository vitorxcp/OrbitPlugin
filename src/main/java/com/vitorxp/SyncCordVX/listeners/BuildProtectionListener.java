package com.vitorxp.SyncCordVX.listeners;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BuildProtectionListener implements Listener {

    private final SyncCordVX plugin;

    public BuildProtectionListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleBuildAttempt(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBuildAttempt(event.getPlayer(), event);
    }

    private void handleBuildAttempt(Player player, org.bukkit.event.Cancellable event) {
        if (!player.hasPermission("orbit.admin.buildmode")) {
            return;
        }

        if (plugin.isInBuildMode(player.getUniqueId())) {
            return;
        }

        if (canBuildWithWorldGuard(player, event instanceof BlockBreakEvent ? ((BlockBreakEvent) event).getBlock().getLocation() : ((BlockPlaceEvent) event).getBlock().getLocation())) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage("§c§lAVISO: Ative o §e/build§c para poder construir/quebrar blocos nesta área.");
    }

    private boolean canBuildWithWorldGuard(Player player, org.bukkit.Location location) {
        RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
        if (regionManager == null) {
            return true;
        }
        ApplicableRegionSet set = regionManager.getApplicableRegions(location);
        return set.canBuild(WGBukkit.getPlugin().wrapPlayer(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removePlayerFromBuildMode(event.getPlayer().getUniqueId());
    }
}