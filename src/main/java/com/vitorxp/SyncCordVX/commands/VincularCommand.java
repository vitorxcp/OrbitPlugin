package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.linking.VincularGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VincularCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public VincularCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser executado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        // Verifica se o jogador já está vinculado
        plugin.getLinkDAO().isLinked(player.getUniqueId()).thenAccept(linked -> {
            if (linked) {
                player.sendMessage("§cVocê já vinculou sua conta!");
            } else {
                // Abre a GUI em um contexto síncrono
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.openInventory(VincularGUI.getInventory(player));
                });
            }
        });

        return true;
    }
}