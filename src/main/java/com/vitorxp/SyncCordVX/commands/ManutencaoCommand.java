package com.vitorxp.SyncCordVX.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ManutencaoCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ManutencaoCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("synccord.manutencao.admin")) {
            sender.sendMessage(ChatColor.RED + "Você не tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
                plugin.getConfig().set("maintenance.enabled", true);
                plugin.saveConfig();
                Bukkit.broadcastMessage(ChatColor.GOLD + "O modo de manutenção foi ATIVADO!");
                sender.sendMessage(ChatColor.GREEN + "Modo de manutenção ativado com sucesso.");
                break;

            case "off":
                plugin.getConfig().set("maintenance.enabled", false);
                plugin.saveConfig();
                Bukkit.broadcastMessage(ChatColor.GOLD + "O modo de manutenção foi DESATIVADO!");
                sender.sendMessage(ChatColor.GREEN + "Modo de manutenção desativado com sucesso.");
                break;

            case "whitelist":
                handleWhitelist(sender, args);
                break;

            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void handleWhitelist(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /manutencao whitelist <add|remove> <player>");
            return;
        }

        String action = args[1].toLowerCase();
        String playerName = args[2];
        List<String> whitelist = plugin.getConfig().getStringList("maintenance.whitelist");

        if (action.equals("add")) {
            if (whitelist.contains(playerName.toLowerCase())) {
                sender.sendMessage(ChatColor.YELLOW + playerName + " já está na whitelist.");
                return;
            }
            whitelist.add(playerName.toLowerCase());
            sender.sendMessage(ChatColor.GREEN + playerName + " foi adicionado à whitelist da manutenção.");
        } else if (action.equals("remove")) {
            if (!whitelist.contains(playerName.toLowerCase())) {
                sender.sendMessage(ChatColor.RED + playerName + " não está na whitelist.");
                return;
            }
            whitelist.remove(playerName.toLowerCase());
            sender.sendMessage(ChatColor.GREEN + playerName + " foi removido da whitelist da manutenção.");
        } else {
            sender.sendMessage(ChatColor.RED + "Uso: /manutencao whitelist <add|remove> <player>");
            return;
        }

        plugin.getConfig().set("maintenance.whitelist", whitelist);
        plugin.saveConfig();
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "--- Ajuda do Modo Manutenção ---");
        sender.sendMessage(ChatColor.GOLD + "/manutencao on " + ChatColor.WHITE + "- Ativa o modo de manutenção.");
        sender.sendMessage(ChatColor.GOLD + "/manutencao off " + ChatColor.WHITE + "- Desativa o modo de manutenção.");
        sender.sendMessage(ChatColor.GOLD + "/manutencao whitelist add <player> " + ChatColor.WHITE + "- Adiciona um jogador à whitelist.");
        sender.sendMessage(ChatColor.GOLD + "/manutencao whitelist remove <player> " + ChatColor.WHITE + "- Remove um jogador da whitelist.");
    }
}