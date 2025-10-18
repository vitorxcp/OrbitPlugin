package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import com.vitorxp.SyncCordVX.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public BanCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.ban")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUso: /ban <nick> <tempo> [motivo]");
            return true;
        }

        String playerName = args[0];
        String timeString = args[1];
        String reason = "Violação das regras";

        if (args.length > 2) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }

        long duration = TimeUtil.parseTime(timeString);

        Player target = Bukkit.getPlayer(playerName);
        UUID targetUUID = target != null ? target.getUniqueId() : null;
        String ip = target != null ? target.getAddress().getAddress().getHostAddress() : null;

        Punishment punishment = new Punishment();
        punishment.setPlayerName(playerName);
        punishment.setPlayerUUID(targetUUID);
        punishment.setIp(ip);
        punishment.setStaffName(sender.getName());
        punishment.setStaffUUID(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
        punishment.setReason(reason);
        punishment.setType(Punishment.Type.BAN);
        punishment.setDuration(duration);
        punishment.setStartTime(System.currentTimeMillis());
        punishment.setActive(true);

        plugin.getPunishmentManager().punish(punishment);
        sender.sendMessage("§aJogador " + playerName + " banido com sucesso.");

        return true;
    }
}