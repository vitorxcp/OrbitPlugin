package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public UnmuteCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.unmute")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso: /unmute <nick>");
            return true;
        }

        String playerName = args[0];
        List<Punishment> activePunishments = plugin.getPunishmentManager().getActivePunishments(playerName, null);

        boolean found = false;
        for (Punishment p : activePunishments) {
            if (p.getType() == Punishment.Type.MUTE) {
                plugin.getPunishmentManager().removePunishment(p, sender.getName(),
                        sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
                found = true;
            }
        }

        if (found) {
            sender.sendMessage("§aJogador " + playerName + " desmutado com sucesso.");
        } else {
            sender.sendMessage("§cNenhum mute ativo encontrado para este jogador.");
        }

        return true;
    }
}