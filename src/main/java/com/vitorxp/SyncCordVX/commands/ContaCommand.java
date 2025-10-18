package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class ContaCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public ContaCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUse /conta <nick> para ver a conta de um jogador.");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("synccordvx.conta.outros")) {
                sender.sendMessage("§cVocê não tem permissão para ver as informações de outros jogadores.");
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || !target.hasPlayedBefore()) {
                sender.sendMessage("§cJogador '" + args[0] + "' não encontrado.");
                return true;
            }
        }
        showAccountInfo(sender, target);
        return true;
    }

    private void showAccountInfo(CommandSender sender, OfflinePlayer target) {
        plugin.getLinkDAO().getDiscordId(target.getUniqueId()).thenAccept(discordId -> {
            if (discordId == null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        sender.sendMessage("§cO jogador §e" + target.getName() + "§c não possui uma conta vinculada.")
                );
                return;
            }

            plugin.getBotManager().retrieveDiscordUser(discordId, discordUser -> {
                String discordTag = discordUser.getAsTag();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§7§m------------------------------------------");
                    sender.sendMessage(" §eInformações da conta de §f" + target.getName());
                    sender.sendMessage("");
                    sender.sendMessage(" §fDiscord: §b" + discordTag);
                    sender.sendMessage(" §fID do Discord: §7" + discordId);
                    sender.sendMessage("§7§m------------------------------------------");
                });

            }, error -> {
                // Se não encontrou o usuário (conta deletada, etc)
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§cNão foi possível encontrar o usuário do Discord com o ID vinculado: " + discordId);
                    sender.sendMessage("§cO jogador pode ter deletado a conta do Discord.");
                });
            });
        });
    }

    private String formatPlaytime(long ticks) {
        long seconds = ticks / 20;
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");

        return sb.length() > 0 ? sb.toString().trim() : "Menos de um minuto";
    }
}