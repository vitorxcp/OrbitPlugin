package com.vitorxp.SyncCordVX.tasks;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.commands.AvisoRestartCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartTask extends BukkitRunnable {

    private final SyncCordVX plugin;
    private int timeLeft;

    public RestartTask(SyncCordVX plugin, int seconds) {
        this.plugin = plugin;
        this.timeLeft = seconds;
    }

    @Override
    public void run() {
        if (timeLeft == 300) {
            broadcastMessage("&cO servidor será reiniciado em 5 minutos!");
        } else if (timeLeft == 180) {
            broadcastMessage("&cO servidor será reiniciado em 3 minutos!");
        } else if (timeLeft == 60) {
            broadcastMessage("&cO servidor será reiniciado em 1 minuto!");
        } else if (timeLeft == 30) {
            broadcastMessage("&cO servidor será reiniciado em 30 segundos!");
        } else if (timeLeft <= 5 && timeLeft > 0) {
            String title = "§cReiniciando em...";
            String subtitle = "§e" + timeLeft;
            broadcastTitle(title, subtitle);
            broadcastMessage("&cO servidor será reiniciado em " + timeLeft + " segundo(s)!");
        }

        if (timeLeft <= 0) {
            AvisoRestartCommand.currentTask = null;

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&cO servidor está reiniciando!\n&aVoltamos em breve."));
            }

            Bukkit.shutdown();

            this.cancel();
            return;
        }

        timeLeft--;
    }

    private void broadcastMessage(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[AVISO] " + message));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
        }
    }

    private void broadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle);
        }
    }
}