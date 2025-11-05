package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.linking.LinkManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LinkListener implements Listener {
    private final SyncCordVX plugin;
    private final LinkManager linkManager;

    public LinkListener(SyncCordVX plugin) {
        this.plugin = plugin;
        this.linkManager = plugin.getLinkManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Vincular Contas")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        if (event.getSlot() == 11) {
            linkManager.startLinkageProcess(player);
        }

        if (event.getSlot() == 15) {
            player.closeInventory();
            player.sendMessage("§9Entre em nosso Discord: §bhttps://discord.gg/DCjbGkcyMb");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        LinkManager.LinkStage stage = linkManager.getPlayerStage(uuid);

        if (stage == LinkManager.LinkStage.NONE) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (stage == LinkManager.LinkStage.AWAITING_DISCORD_ID) {
                linkManager.receivedDiscordId(player, message);
            } else if (stage == LinkManager.LinkStage.AWAITING_CODE) {
                linkManager.receivedVerificationCode(player, message);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        linkManager.cancelLinkage(event.getPlayer().getUniqueId());
    }
}