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

public class MuteCommand implements CommandExecutor {

    private SyncCordVX plugin;

    public MuteCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccordvx.mute")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUso: /mute <nick> <tempo> [motivo]");
            sender.sendMessage("§cExemplo: /mute Player 1d Hack");
            sender.sendMessage("§cTempo: 1d (1 dia), 2h (2 horas), 30m (30 minutos)");
            return true;
        }

        String playerName = args[0];
        String timeString = args[1];
        String reason = "Violação das regras do chat";

        if (args.length > 2) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }

        long duration = TimeUtil.parseTime(timeString);
        if (duration == 0) {
            sender.sendMessage("§cTempo inválido! Use: 1d, 2h, 30m, etc.");
            return true;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado ou offline.");
            return true;
        }

        // Verifica se o jogador já está mutado
        if (plugin.getPunishmentManager().isMuted(target.getName(), target.getUniqueId())) {
            sender.sendMessage("§cEste jogador já está mutado.");
            return true;
        }

        Punishment punishment = new Punishment();
        punishment.setPlayerName(target.getName());
        punishment.setPlayerUUID(target.getUniqueId());
        punishment.setIp(target.getAddress().getAddress().getHostAddress());
        punishment.setStaffName(sender.getName());
        punishment.setStaffUUID(sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"));
        punishment.setReason(reason);
        punishment.setType(Punishment.Type.MUTE);
        punishment.setDuration(duration);
        punishment.setStartTime(System.currentTimeMillis());
        punishment.setActive(true);

        plugin.getPunishmentManager().punish(punishment);

        // Envia mensagem para o jogador mutado
        String muteMessage = plugin.getConfigManager().getMuteMessage(punishment);
        target.sendMessage(muteMessage);

        sender.sendMessage("§aJogador " + target.getName() + " mutado com sucesso por " + TimeUtil.formatTime(duration) + ".");

        // Broadcast para staffs
        Bukkit.broadcast("§c[STAFF] §f" + target.getName() + " foi mutado por " + sender.getName() + " por " + reason, "synccordvx.staff");

        return true;
    }
}