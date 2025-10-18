package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildModeCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public BuildModeCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("orbit.admin.buildmode")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        boolean isInBuildMode = plugin.toggleBuildMode(player.getUniqueId());

        if (isInBuildMode) {
            player.sendMessage(ChatColor.GREEN + "Modo Construção Ativado. Agora você pode construir livremente.");
        } else {
            player.sendMessage(ChatColor.RED + "Modo Construção Desativado. A proteção contra quebra de blocos no criativo está ativa.");
        }

        return true;
    }
}