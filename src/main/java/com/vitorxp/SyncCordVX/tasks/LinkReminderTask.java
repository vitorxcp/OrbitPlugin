package com.vitorxp.SyncCordVX.tasks;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.database.LinkDAO;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LinkReminderTask extends BukkitRunnable {

    private final SyncCordVX plugin;

    public LinkReminderTask(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        LinkDAO linkDAO = plugin.getLinkDAO();
        if (linkDAO == null) return;

        // 1. Pega a lista de jogadores de forma SEGURA na thread principal
        for (Player player : Bukkit.getOnlinePlayers()) {

            // 2. Para CADA jogador, dispara a verificação assíncrona no banco de dados
            linkDAO.isLinked(player.getUniqueId()).thenAcceptAsync(isLinked -> {

                // 3. O código aqui dentro roda de forma ASSÍNCRONA quando o DB responde
                if (!isLinked) {

                    // 4. Volta para a thread PRINCIPAL para enviar a mensagem ao jogador
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            sendMessage(player);
                        }
                    });
                }
            });
        }
    }

    private void sendMessage(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§8§m----------------------------------------------------");
        player.sendMessage(" ");
        player.sendMessage("§e§lPROTEJA SUA CONTA!");
        player.sendMessage("§7Vimos que sua conta do Minecraft ainda não está");
        player.sendMessage("§7vinculada à sua conta do Discord.");
        player.sendMessage(" ");
        player.sendMessage("§fPara aumentar a segurança, use: §b/vincular");
        player.sendMessage(" ");
        player.sendMessage("§8§m----------------------------------------------------");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
    }
}