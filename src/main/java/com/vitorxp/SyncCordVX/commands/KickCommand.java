package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public KickCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.kick")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUso: /kick <nick> [motivo]");
            return true;
        }

        String playerName = args[0];
        String reason = "Violação das regras";

        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return true;
        }

        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(target.getUniqueId());
        punishment.setIp(target.getAddress().getAddress().getHostAddress());
        punishment.setStaffName(sender.getName());
        punishment.setStaffUUID(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
        punishment.setReason(reason);
        punishment.setType(Punishment.Type.KICK);
        punishment.setDuration(-1);
        punishment.setStartTime(System.currentTimeMillis());
        punishment.setActive(false);

        plugin.getPunishmentManager().punish(punishment);
        sender.sendMessage("§aJogador " + playerName + " kickado com sucesso.");

        return true;
    }
}