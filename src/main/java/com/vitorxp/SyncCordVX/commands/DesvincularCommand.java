package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DesvincularCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public DesvincularCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando só pode ser usado por jogadores. Para desvincular outros, use /desvincular <nick>.");
                return true;
            }
            Player player = (Player) sender;
            handleUnlink(player, player.getUniqueId());
            return true;
        }

        if (!sender.hasPermission("synccordvx.desvincular.outros")) {
            sender.sendMessage("§cVocê não tem permissão para desvincular a conta de outros jogadores.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage("§cJogador '" + args[0] + "' não encontrado.");
            return true;
        }
        handleUnlink(sender, target.getUniqueId());

        return true;
    }

    private void handleUnlink(CommandSender sender, UUID targetUUID) {
        plugin.getLinkDAO().unlinkAccount(targetUUID).thenAccept(success -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    if (sender instanceof Player && ((Player) sender).getUniqueId().equals(targetUUID)) {
                        sender.sendMessage("§aSua conta foi desvinculada com sucesso!");
                    } else {
                        sender.sendMessage("§aA conta de §e" + Bukkit.getOfflinePlayer(targetUUID).getName() + "§a foi desvinculada com sucesso!");
                    }
                } else {
                    sender.sendMessage("§cEsta conta não estava vinculada ou ocorreu um erro.");
                }
            });
        });
    }
}