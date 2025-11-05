package com.vitorxp.SyncCordVX.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class testCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public testCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser executado por jogadores.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("§eCalculando o Top 3 jogadores mais ricos do servidor...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            File dataFile = new File(plugin.getDataFolder().getParentFile(), "JH_Economy/contas.save");

            if (!dataFile.exists()) {
                player.sendMessage("§cNão foi possível encontrar o arquivo de dados da economia.");
                return;
            }

            HashMap<String, Double> allBalances = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] parts = line.split(":");
                        if (parts.length < 2) continue;

                        String playerName = parts[0];
                        String balancePart = parts[1].split(";")[0];
                        double balance = Double.parseDouble(balancePart);

                        allBalances.put(playerName, balance);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        plugin.getLogger().warning("Não foi possível ler a linha do baltop: " + line);
                    }
                }
            } catch (IOException e) {
                player.sendMessage("§cOcorreu um erro ao ler o arquivo de dados da economia.");
                e.printStackTrace();
                return;
            }

            List<Map.Entry<String, Double>> top3 = allBalances.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§6§l========== §eTOP 3 - MAIS RICOS §6§l==========");
                if (top3.isEmpty()) {
                    player.sendMessage("§cNão há jogadores no ranking.");
                } else {
                    int rank = 1;
                    for (Map.Entry<String, Double> entry : top3) {
                        String playerName = capitalize(entry.getKey());
                        double balance = entry.getValue();
                        String formattedBalance = String.format("%,.2f", balance);

                        player.sendMessage(" §e" + rank + "º. §f" + playerName + " §7- §a$" + formattedBalance);
                        rank++;
                    }
                }
                player.sendMessage("§6§l======================================");
            });
        });

        return true;
    }

    // Método simples para deixar a primeira letra do nome maiúscula
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}