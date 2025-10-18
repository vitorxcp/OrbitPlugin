package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import com.vitorxp.SyncCordVX.utils.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class PlayerChatListener implements Listener {

    private SyncCordVX plugin;

    public PlayerChatListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPunishmentManager().isMuted(player.getName(), player.getUniqueId())) {
            List<Punishment> activePunishments = plugin.getPunishmentManager().getActivePunishments(player.getName(), player.getUniqueId());

            for (Punishment punishment : activePunishments) {
                if (punishment.getType() == Punishment.Type.MUTE && punishment.isActive()) {
                    if (punishment.isExpired()) {
                        plugin.getPunishmentManager().removePunishment(punishment, "Sistema",
                                java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                        return;
                    }

                    event.setCancelled(true);

                    String muteMessage = buildMuteMessage(punishment);
                    player.sendMessage(muteMessage);
                    return;
                }
            }
        }
    }

    private String buildMuteMessage(Punishment punishment) {
        String baseMessage = plugin.getConfigManager().getMuteMessage(punishment);

        if (baseMessage == null || baseMessage.isEmpty()) {
            baseMessage = "§cVocê está mutado!\n§fMotivo: §e%reason%\n§fTempo restante: §e%time%";
        }

        // Calcula tempo restante
        long remainingTime = punishment.getEndTime() - System.currentTimeMillis();
        String timeFormatted = punishment.isPermanent() ? "Permanente" : TimeUtil.formatTime(remainingTime);

        return baseMessage
                .replace("%player%", punishment.getPlayerName())
                .replace("%reason%", punishment.getReason())
                .replace("%staff%", punishment.getStaffName())
                .replace("%time%", timeFormatted)
                .replace("%date%", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                        .format(java.time.Instant.ofEpochMilli(punishment.getStartTime()).atZone(java.time.ZoneId.systemDefault())));
    }
}