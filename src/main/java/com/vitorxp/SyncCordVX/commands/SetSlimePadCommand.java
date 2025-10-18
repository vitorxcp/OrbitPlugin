package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.pads.SlimePadManager; // Adicione esta importação
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SetSlimePadCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public SetSlimePadCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("orbit.slimepad.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("add") && !args[0].equalsIgnoreCase("remove"))) {
            sender.sendMessage("§cUse: /setslimepad <add|remove>");
            return true;
        }

        Player player = (Player) sender;
        Block targetBlock = null;

        Location eyeLocation = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();

        for (double i = 0.5; i < 5.0; i += 0.5) { // Ajuste fino no passo do raio
            Location currentLocation = eyeLocation.clone().add(direction.clone().multiply(i));
            Block currentBlock = currentLocation.getBlock();
            if (currentBlock.getType() != Material.AIR) {
                targetBlock = currentBlock;
                break;
            }
        }

        if (targetBlock == null || targetBlock.getType() != Material.SLIME_BLOCK) {
            Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
            if (blockBelow.getType() == Material.SLIME_BLOCK) {
                targetBlock = blockBelow;
            }
        }

        if (targetBlock == null || targetBlock.getType() != Material.SLIME_BLOCK) {
            player.sendMessage("§cVocê precisa estar olhando para um bloco de slime OU em cima de um.");
            return true;
        }

        // --- CORREÇÃO PRINCIPAL AQUI ---
        // Pega o gerenciador diretamente do plugin principal
        SlimePadManager manager = plugin.getSlimePadManager();
        if (manager == null) {
            player.sendMessage("§cErro: O SlimePadManager não foi carregado corretamente.");
            return true;
        }
        // --- FIM DA CORREÇÃO ---

        if (args[0].equalsIgnoreCase("add")) {
            manager.addSlimePad(targetBlock.getLocation());
            player.sendMessage("§aSlime pad adicionado com sucesso na localização: " + formatLocation(targetBlock.getLocation()));
        } else {
            manager.removeSlimePad(targetBlock.getLocation());
            player.sendMessage("§cSlime pad removido com sucesso da localização: " + formatLocation(targetBlock.getLocation()));
        }

        return true;
    }

    private String formatLocation(Location loc) {
        return "X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ();
    }
}