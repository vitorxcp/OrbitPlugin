package com.vitorxp.SyncCordVX.managers;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.database.DatabaseManager;
import com.vitorxp.SyncCordVX.database.PunishmentDAO;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentManager {

    private SyncCordVX plugin;
    private DatabaseManager databaseManager;
    private PunishmentDAO punishmentDAO;

    public PunishmentManager(SyncCordVX plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.punishmentDAO = new PunishmentDAO(databaseManager);
    }

    public void punish(Punishment punishment) {
        punishmentDAO.addPunishment(punishment);

        Player player = Bukkit.getPlayer(punishment.getPlayerUUID());
        if (player != null) {
            applyPunishment(player, punishment);
        }

        plugin.getBotManager().sendPunishmentEmbed(punishment);
    }

    public void applyPunishment(Player player, Punishment punishment) {
        switch (punishment.getType()) {
            case BAN:
            case BAN_IP:
                String banMessage = plugin.getConfigManager().getBanMessage(punishment);
                player.kickPlayer(banMessage);
                break;
            case MUTE:
                break;
            case KICK:
                String kickMessage = plugin.getConfigManager().getKickMessage(punishment);
                player.kickPlayer(kickMessage);
                break;
        }
    }

    public void removePunishment(Punishment punishment, String staffName, UUID staffUUID) {
        punishmentDAO.removePunishment(punishment.getId(), staffName, staffUUID, System.currentTimeMillis());
        plugin.getBotManager().sendPunishmentRemoveEmbed(punishment);
    }

    public List<Punishment> getActivePunishments(String playerName, UUID playerUUID) {
        List<Punishment> punishmentsFromDB = punishmentDAO.getActivePunishmentsByPlayer(playerName, playerUUID);
        return filterAndDeactivateExpired(punishmentsFromDB);
    }

    public List<Punishment> getActivePunishmentsByIP(String ip) {
        List<Punishment> punishmentsFromDB = punishmentDAO.getActivePunishmentsByIP(ip);
        return filterAndDeactivateExpired(punishmentsFromDB);
    }

    public List<Punishment> getPunishmentHistory(String playerName, UUID playerUUID) {
        return punishmentDAO.getPunishmentHistory(playerName, playerUUID);
    }

    public List<Punishment> getAllActivePunishments() {
        List<Punishment> punishmentsFromDB = punishmentDAO.getAllActivePunishments();
        return filterAndDeactivateExpired(punishmentsFromDB);
    }

    private List<Punishment> filterAndDeactivateExpired(List<Punishment> punishments) {
        List<Punishment> expired = new ArrayList<>();
        List<Punishment> stillActive = new ArrayList<>();

        for (Punishment p : punishments) {
            if (p.isExpired()) {
                expired.add(p);
            } else {
                stillActive.add(p);
            }
        }

        if (!expired.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Punishment p : expired) {
                    punishmentDAO.deactivatePunishment(p.getId());
                }
            });
        }

        return stillActive;
    }


    public boolean isBanned(String playerName, UUID playerUUID) {
        for (Punishment p : getActivePunishments(playerName, playerUUID)) {
            if (p.getType() == Punishment.Type.BAN) {
                return true;
            }
        }
        return false;
    }

    public boolean isIPBanned(String ip) {
        for (Punishment p : getActivePunishmentsByIP(ip)) {
            if (p.getType() == Punishment.Type.BAN_IP) {
                return true;
            }
        }
        return false;
    }

    public boolean isMuted(String playerName, UUID playerUUID) {
        for (Punishment p : getActivePunishments(playerName, playerUUID)) {
            if (p.getType() == Punishment.Type.MUTE) {
                return true;
            }
        }
        return false;
    }
}