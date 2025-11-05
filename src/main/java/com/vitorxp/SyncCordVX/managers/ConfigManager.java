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
                .replace("%tempo%", formatTime(punishment.getDuration()));
    }

    private String formatTime(long duration) {
        if (duration == -1) {
            return "Permanente";
        }
        return "Temporário";
    }

    public ConfigurationSection getDiscordEmbedAddPunishment() {
        return plugin.getConfig().getConfigurationSection("discord.embeds.addpunishments");
    }

    public ConfigurationSection getDiscordEmbedRemovePunishment() {
        return plugin.getConfig().getConfigurationSection("discord.embeds.removepunishments");
    }
}