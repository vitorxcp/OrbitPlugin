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
        // Envia avisos em intervalos de tempo específicos
        if (timeLeft == 300) { // 5 minutos
            broadcastMessage("&cO servidor será reiniciado em 5 minutos!");
        } else if (timeLeft == 180) { // 3 minutos
            broadcastMessage("&cO servidor será reiniciado em 3 minutos!");
        } else if (timeLeft == 60) { // 1 minuto
            broadcastMessage("&cO servidor será reiniciado em 1 minuto!");
        } else if (timeLeft == 30) { // 30 segundos
            broadcastMessage("&cO servidor será reiniciado em 30 segundos!");
        } else if (timeLeft <= 10 && timeLeft > 0) {
            // Contagem regressiva final com título na tela
            String title = "§cReiniciando em...";
            String subtitle = "§e" + timeLeft;
            broadcastTitle(title, subtitle);
            broadcastMessage("&cO servidor será reiniciado em " + timeLeft + " segundo(s)!");
        }

        // Quando o tempo acaba
        if (timeLeft <= 0) {
            // Limpa a referência da tarefa
            AvisoRestartCommand.currentTask = null;

            // Kicka todos os jogadores com uma mensagem
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&cO servidor está reiniciando!\n&aVoltamos em breve."));
            }

            // Desliga o servidor
            Bukkit.shutdown();

            // Cancela esta tarefa
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
            // API de Títulos para 1.8.8
            player.sendTitle(title, subtitle);
        }
    }
}