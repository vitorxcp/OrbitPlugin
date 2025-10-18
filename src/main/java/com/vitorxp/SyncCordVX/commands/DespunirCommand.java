package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DespunirCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public DespunirCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.despunir")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso: /despunir <nick>");
            return true;
        }

        String playerName = args[0];
        List<Punishment> activePunishments = plugin.getPunishmentManager().getActivePunishments(playerName, null);

        int removed = 0;
        for (Punishment p : activePunishments) {
            plugin.getPunishmentManager().removePunishment(p, sender.getName(),
                    sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));

            removed++;
        }

        if (removed > 0) {
            sender.sendMessage("§a" + removed + " punições removidas do jogador " + playerName + ".");
        } else {
            sender.sendMessage("§cNenhuma punição ativa encontrada para este jogador.");
        }

        return true;
    }
}