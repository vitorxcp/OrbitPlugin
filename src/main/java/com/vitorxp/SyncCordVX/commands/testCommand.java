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

        // Executa a leitura do arquivo fora da thread principal para garantir performance
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Constrói o caminho para o arquivo de contas do JH_Economy
            File dataFile = new File(plugin.getDataFolder().getParentFile(), "JH_Economy/contas.save");

            if (!dataFile.exists()) {
                player.sendMessage("§cNão foi possível encontrar o arquivo de dados da economia.");
                return;
            }

            HashMap<String, Double> allBalances = new HashMap<>();

            // Usa try-with-resources para garantir que o leitor de arquivo seja fechado
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        // O formato é: nome:saldo;NomeOriginal
                        String[] parts = line.split(":");
                        if (parts.length < 2) continue; // Pula linhas mal formatadas

                        String playerName = parts[0];
                        // Pega a parte do saldo, mesmo que tenha o nome original depois do ';'
                        String balancePart = parts[1].split(";")[0];
                        double balance = Double.parseDouble(balancePart);

                        allBalances.put(playerName, balance);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        // Ignora linhas que não consiga interpretar para não quebrar o comando
                        plugin.getLogger().warning("Não foi possível ler a linha do baltop: " + line);
                    }
                }
            } catch (IOException e) {
                player.sendMessage("§cOcorreu um erro ao ler o arquivo de dados da economia.");
                e.printStackTrace();
                return;
            }

            // A partir daqui, a lógica de ordenar e exibir é a mesma de antes
            List<Map.Entry<String, Double>> top3 = allBalances.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            // Volta para a thread principal para enviar a mensagem ao jogador (obrigatório)
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§6§l========== §eTOP 3 - MAIS RICOS §6§l==========");
                if (top3.isEmpty()) {
                    player.sendMessage("§cNão há jogadores no ranking.");
                } else {
                    int rank = 1;
                    for (Map.Entry<String, Double> entry : top3) {
                        // O nome já está em minúsculas, vamos capitalizar para ficar mais bonito
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