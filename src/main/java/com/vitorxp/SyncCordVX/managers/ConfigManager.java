package com.vitorxp.SyncCordVX.managers;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private SyncCordVX plugin;

    public ConfigManager(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    public List<String> getReasons() {
        return plugin.getConfig().getStringList("motivos");
    }

    public String getBanMessage(Punishment punishment) {
        String message = plugin.getConfig().getString("mensagens.ban");
        return replacePlaceholders(message, punishment);
    }

    public String getKickMessage(Punishment punishment) {
        String message = plugin.getConfig().getString("mensagens.kick");
        return replacePlaceholders(message, punishment);
    }

    public String getMuteMessage(Punishment punishment) {
        String message = plugin.getConfig().getString("mensagens.mute");
        return replacePlaceholders(message, punishment);
    }

    private String replacePlaceholders(String message, Punishment punishment) {
        return message
                .replace("%player%", punishment.getPlayerName())
                .replace("%reason%", punishment.getReason())
                .replace("%staff%", punishment.getStaffName())
                .replace("%tempo%", formatTime(punishment.getDuration()))
                .replace("%date%", formatDate(punishment.getStartTime()))
                .replace("%time%", formatTime(punishment.getDuration()));
    }

    private String formatTime(long duration) {
        if (duration == -1) {
            return "Permanente";
        }

        if (duration > 1000000000L) {
            duration /= 1000;
        } else if (duration > 1000) {
            duration /= 1000;
        }

        long days = duration / 86400;
        long hours = (duration % 86400) / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }


    private String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return "Data inválida";
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.util.Date date = new java.util.Date(timestamp);
        return sdf.format(date);
    }


    public ConfigurationSection getDiscordEmbedAddPunishment() {
        return plugin.getConfig().getConfigurationSection("discord.embeds.addpunishments");
    }

    public ConfigurationSection getDiscordEmbedRemovePunishment() {
        return plugin.getConfig().getConfigurationSection("discord.embeds.removepunishments");
    }
}