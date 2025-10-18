package com.vitorxp.SyncCordVX.commands;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

public class SyncCordCommand implements CommandExecutor {

    private final SyncCordVX plugin;

    public SyncCordCommand(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("orbit.synccord.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "forcereload":
                handleForceReload(sender, args);
                break;
            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadAllConfigs();
        sender.sendMessage("§aAs configurações do SyncCordVX foram recarregadas com sucesso!");
        sender.sendMessage("§7(Nota: Para atualizar os NPCs, use o comando de reload específico deles).");
    }

    private void handleForceReload(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirmar")) {
            sender.sendMessage("§c§lAVISO: §cEsta é uma operação perigosa que pode causar erros graves.");
            sender.sendMessage("§cUse apenas se souber o que está fazendo. A reinicialização completa do servidor é sempre recomendada.");
            sender.sendMessage("§ePara confirmar, use: /synccord forcereload confirmar");
            return;
        }

        sender.sendMessage("§eIniciando recarregamento forçado do SyncCordVX... (Isso pode causar erros)");
        PluginManager pm = plugin.getServer().getPluginManager();
        try {
            pm.disablePlugin(plugin);
            pm.enablePlugin(plugin);
            sender.sendMessage("§aPlugin recarregado. Verifique o console por erros.");
        } catch (Exception e) {
            sender.sendMessage("§cOcorreu um erro grave durante o recarregamento. Verifique o console.");
            e.printStackTrace();
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§b§lSyncCordVX §f- Ajuda");
        sender.sendMessage(" §f/synccord reload §7- Recarrega os arquivos de configuração (seguro).");
        sender.sendMessage(" §f/synccord forcereload §7- Tenta recarregar o plugin inteiro (perigoso).");
    }
}