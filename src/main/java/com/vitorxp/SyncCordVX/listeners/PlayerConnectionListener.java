package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.managers.PunishmentManager;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private SyncCordVX plugin;

    public PlayerConnectionListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PunishmentManager punishmentManager = plugin.getPunishmentManager();

        if (punishmentManager.isBanned(player.getName(), player.getUniqueId())) {
            List<Punishment> bans = punishmentManager.getActivePunishments(player.getName(), player.getUniqueId());
            Punishment ban = bans.stream().filter(p -> p.getType() == Punishment.Type.BAN).findFirst().orElse(null);
            if (ban != null) {
                String message = plugin.getConfigManager().getBanMessage(ban);
                event.setKickMessage(message);
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                return;
            }
        }

        String ip = event.getAddress().getHostAddress();
        if (punishmentManager.isIPBanned(ip)) {
            List<Punishment> ipBans = punishmentManager.getActivePunishmentsByIP(ip);
            Punishment ipBan = ipBans.stream().filter(p -> p.getType() == Punishment.Type.BAN_IP).findFirst().orElse(null);
            if (ipBan != null) {
                String message = plugin.getConfigManager().getBanMessage(ipBan);
                event.setKickMessage(message);
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            }
        }

        boolean maintenanceEnabled = plugin.getConfig().getBoolean("maintenance.enabled", false);

        if (!maintenanceEnabled) {
            return;
        }
        if (player.hasPermission("synccordvx.manutencao.bypass")) {
            return;
        }

        List<String> whitelist = plugin.getConfig().getStringList("maintenance.whitelist");
        if (whitelist.contains(player.getName().toLowerCase())) {
            return;
        }

        String kickMessage = plugin.getConfig().getString("maintenance.kick-message", "&cO servidor está em manutenção.");
        kickMessage = ChatColor.translateAlternateColorCodes('&', kickMessage);

        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
    }
}