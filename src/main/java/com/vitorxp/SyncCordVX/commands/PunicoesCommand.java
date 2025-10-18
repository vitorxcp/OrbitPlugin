package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.gui.PunicoesAtivasGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunicoesCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public PunicoesCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("synccordvx.punicoes")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        player.openInventory(PunicoesAtivasGUI.getInventory(plugin));
        return true;
    }
}