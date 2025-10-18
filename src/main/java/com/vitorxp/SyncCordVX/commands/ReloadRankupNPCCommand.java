package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.npcs.rankup.RankupNPCManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadRankupNPCCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public ReloadRankupNPCCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orbit.reload.rankupnpcs")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        RankupNPCManager npcManager = plugin.getRankupNpcManager();
        if (npcManager == null) {
            sender.sendMessage(ChatColor.RED + "O sistema de NPCs de RankUp não está ativo.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Forçando a atualização dos NPCs de ranking (RankUp)...");
        npcManager.updateRankupNpcs();
        sender.sendMessage(ChatColor.GREEN + "Atualização iniciada com sucesso!");

        return true;
    }
}