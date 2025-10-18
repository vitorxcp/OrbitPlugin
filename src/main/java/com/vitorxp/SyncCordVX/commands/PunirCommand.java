package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.gui.PunirGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunirCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public PunirCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("synccordvx.punir")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUso: /punir <nick>");
            player.sendMessage("§cIsso abrirá uma GUI para punir o jogador.");
            return true;
        }

        String targetName = args[0];

        // Verifica se o jogador existe (online ou offline)
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            // Poderia verificar no banco de dados se o jogador já jogou antes
            player.sendMessage("§cJogador não encontrado. Verifique se o nome está correto.");
            return true;
        }

        // Abre a GUI de punição
        player.openInventory(PunirGUI.getInventory(targetName));
        player.sendMessage("§aAbrindo menu de punições para §f" + targetName);

        return true;
    }
}