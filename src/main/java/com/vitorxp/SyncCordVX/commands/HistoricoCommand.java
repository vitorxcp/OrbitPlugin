package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.gui.HistoricoGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HistoricoCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public HistoricoCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("synccordvx.historico")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUso: /historico <nick>");
            player.sendMessage("§cIsso mostrará o histórico de punições do jogador.");
            return true;
        }

        String targetName = args[0];

        // Busca o UUID do jogador (online ou offline)
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage("§cJogador não encontrado.");
            return true;
        }

        // Abre a GUI do histórico
        player.openInventory(HistoricoGUI.getInventory(plugin, targetName));
        player.sendMessage("§aCarregando histórico de punições de §f" + targetName);

        return true;
    }

    private UUID getPlayerUUID(String playerName) {
        // Primeiro tenta encontrar online
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // Depois tenta encontrar nos jogadores offline
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }
}