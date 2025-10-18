package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.npcs.NPCManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadNPCsCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public ReloadNPCsCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orbit.reload.npcs")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        NPCManager npcManager = plugin.getNpcManager();
        if (npcManager == null) {
            sender.sendMessage(ChatColor.RED + "O sistema de NPCs de rank não está ativo. Verifique se os plugins Citizens e HolographicDisplays estão carregados.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Forçando a atualização dos NPCs de ranking...");

        npcManager.updateRankedNpcs();

        sender.sendMessage(ChatColor.GREEN + "Atualização iniciada com sucesso!");
        sender.sendMessage(ChatColor.GRAY + "Os NPCs e hologramas serão atualizados em alguns instantes.");

        return true;
    }
}