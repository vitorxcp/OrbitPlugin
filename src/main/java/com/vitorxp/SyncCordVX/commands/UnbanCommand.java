package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public UnbanCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.unban")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso: /unban <nick>");
            return true;
        }

        String playerName = args[0];
        List<Punishment> activePunishments = plugin.getPunishmentManager().getActivePunishments(playerName, null);

        boolean found = false;
        for (Punishment p : activePunishments) {
            if (p.getType() == Punishment.Type.BAN) {
                plugin.getPunishmentManager().removePunishment(p, sender.getName(),
                        sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
                found = true;
            }
        }

        if (found) {
            sender.sendMessage("§aJogador " + playerName + " desbanido com sucesso.");
        } else {
            sender.sendMessage("§cNenhum ban ativo encontrado para este jogador.");
        }

        return true;
    }
}