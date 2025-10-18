package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.tasks.RestartTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public class AvisoRestartCommand implements CommandExecutor {

    private final SyncCordVX plugin;
    public static BukkitTask currentTask = null; // Guarda a tarefa de reinício atual

    public AvisoRestartCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orbit.restart.avisar")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Uso correto: /aviso-restart <tempo> ou /aviso-restart cancelar");
            return true;
        }

        // Comando para cancelar um reinício agendado
        if (args[0].equalsIgnoreCase("cancelar")) {
            if (currentTask == null) {
                sender.sendMessage(ChatColor.RED + "Nenhum reinício está agendado para ser cancelado.");
                return true;
            }

            currentTask.cancel();
            currentTask = null;
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a&l[AVISO] &aO reinício agendado foi cancelado!"));
            return true;
        }

        // Se já houver um reinício agendado
        if (currentTask != null) {
            sender.sendMessage(ChatColor.RED + "Um reinício já está em andamento. Use /aviso-restart cancelar primeiro.");
            return true;
        }

        try {
            int seconds = parseTime(args[0]);
            if (seconds <= 0) {
                sender.sendMessage(ChatColor.RED + "Tempo inválido. Use formatos como: 10s, 5m, 1h");
                return true;
            }

            RestartTask task = new RestartTask(plugin, seconds);
            currentTask = task.runTaskTimer(plugin, 0L, 20L);

            sender.sendMessage(ChatColor.GREEN + "Agendamento de reinício iniciado com sucesso!");

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    /**
     * Converte uma string de tempo (ex: "5m") para segundos.
     */
    private int parseTime(String timeString) {
        timeString = timeString.toLowerCase();
        int value = Integer.parseInt(timeString.replaceAll("[^0-9]", ""));

        if (timeString.endsWith("s")) {
            return value;
        } else if (timeString.endsWith("m")) {
            return value * 60;
        } else if (timeString.endsWith("h")) {
            return value * 3600;
        } else {
            throw new IllegalArgumentException("Formato de tempo inválido. Use 's' para segundos, 'm' para minutos, ou 'h' para horas.");
        }
    }
}