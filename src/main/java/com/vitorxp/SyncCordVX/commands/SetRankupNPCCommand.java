package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRankupNPCCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public SetRankupNPCCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("orbit.setrankupnpc")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("§cUse: /setrankupnpc <top1|top2|top3>");
            return true;
        }

        Player player = (Player) sender;
        String arg = args[0].toLowerCase();
        int rank;

        if (arg.equals("top1")) rank = 1;
        else if (arg.equals("top2")) rank = 2;
        else if (arg.equals("top3")) rank = 3;
        else {
            sender.sendMessage("§cUse: /setrankupnpc <top1|top2|top3>");
            return true;
        }

        if (plugin.getRankupNpcManager() != null) {
            plugin.getRankupNpcManager().setNpcLocation(rank, player.getLocation());
            sender.sendMessage("§aNPC do Top Rank " + rank + " definido na sua localização atual!");
        } else {
            sender.sendMessage("§cO sistema de NPCs de RankUp não está ativo.");
        }
        return true;
    }
}