package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.chunks.ChunkManager;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KeepChunkCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public KeepChunkCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orbit.admin.keepchunk")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return true;
        }
        if (args.length != 1 || (!args[0].equalsIgnoreCase("add") && !args[0].equalsIgnoreCase("remove"))) {
            sender.sendMessage("§cUse: /keepchunk <add|remove>");
            return true;
        }

        Player player = (Player) sender;
        Chunk chunk = player.getLocation().getChunk();
        ChunkManager manager = plugin.getChunkManager();

        if (args[0].equalsIgnoreCase("add")) {
            if (manager.addChunk(chunk)) {
                player.sendMessage("§aChunk (" + chunk.getX() + ", " + chunk.getZ() + ") adicionado à lista para ser mantido carregado.");
            } else {
                player.sendMessage("§eEste chunk já estava na lista.");
            }
        } else {
            if (manager.removeChunk(chunk)) {
                player.sendMessage("§cChunk (" + chunk.getX() + ", " + chunk.getZ() + ") removido da lista.");
            } else {
                player.sendMessage("§eEste chunk não estava na lista.");
            }
        }
        return true;
    }
}