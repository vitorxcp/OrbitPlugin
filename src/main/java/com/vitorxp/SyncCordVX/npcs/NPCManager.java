package com.vitorxp.SyncCordVX.npcs;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.vitorxp.SyncCordVX.SyncCordVX;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NPCManager {

    private final SyncCordVX plugin;
    private final Map<Integer, NPC> rankedNpcs = new HashMap<>();
    private final Map<Integer, Hologram> rankedHolograms = new HashMap<>();
    private FileConfiguration locationsConfig;
    private File locationsFile;

    public NPCManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupLocationsFile();
        loadNpcs();
    }

    public void setNpcLocation(int rank, Location location) {
        // Se já existe um NPC para este rank, removemos o antigo primeiro para evitar duplicatas
        if (rankedNpcs.containsKey(rank)) {
            rankedNpcs.get(rank).destroy();
        }
        if (rankedHolograms.containsKey(rank)) {
            rankedHolograms.get(rank).delete();
        }

        // Cria um novo NPC
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§e#" + rank + " - Carregando...");
        npc.spawn(location);
        npc.setProtected(true);

        // Salva a localização E o ID do novo NPC no arquivo de configuração
        String path = "npcs.top" + rank;
        locationsConfig.set(path + ".location", location);
        locationsConfig.set(path + ".id", npc.getId());
        saveLocations();

        // Armazena o novo NPC e cria seu holograma
        rankedNpcs.put(rank, npc);

        Location hologramLocation = location.clone().add(0, 2.3, 0);
        Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
        hologram.appendTextLine("§e§lTOP #" + rank);
        rankedHolograms.put(rank, hologram);

        // Dispara uma atualização imediata para o NPC recém-criado
        updateRankedNpcs();
    }

    public void updateRankedNpcs() {
        File dataFile = new File(plugin.getDataFolder().getParentFile(), "JH_Economy/contas.save");
        if (!dataFile.exists()) return;

        Map<String, Double> allBalances = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(":");
                    if (parts.length < 2) continue;
                    String balancePart = parts[1].split(";")[0];
                    allBalances.put(parts[0], Double.parseDouble(balancePart));
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Map.Entry<String, Double>> top3 = allBalances.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int i = 1; i <= 3; i++) {
                NPC npc = rankedNpcs.get(i);
                Hologram hologram = rankedHolograms.get(i);
                if (npc == null || hologram == null) continue;

                if (top3.size() >= i) {
                    Map.Entry<String, Double> entry = top3.get(i - 1);
                    String playerName = capitalize(entry.getKey());
                    double balance = entry.getValue();
                    String formattedBalance = String.format("%,.2f", balance);

                    npc.setName("§e#" + i + " - " + playerName);
                    updateNpcSkin(npc, playerName);

                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i);
                    hologram.appendTextLine("§f" + playerName);
                    hologram.appendTextLine("§a$" + formattedBalance);
                } else {
                    npc.setName("§7#" + i + " - Vazio");
                    updateNpcSkin(npc, "Steve");

                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i);
                    hologram.appendTextLine("§7- Vazio -");
                }
            }
        });
    }

    private void updateNpcSkin(NPC npc, String skinName) {
        String currentSkin = npc.data().get("player-skin-name");
        if (currentSkin != null && currentSkin.equalsIgnoreCase(skinName)) {
            return;
        }

        npc.data().set("player-skin-name", skinName);

        if (npc.isSpawned()) {
            Location loc = npc.getStoredLocation();
            npc.despawn();
            npc.spawn(loc);
        }
    }

    public void shutdown() {
        // NÃO destrua os NPCs. Apenas limpe as referências e os hologramas.
        // O Citizens cuidará de salvar e recarregar os NPCs fisicamente.
        if (!rankedHolograms.isEmpty()) {
            rankedHolograms.values().forEach(Hologram::delete);
        }
        rankedNpcs.clear();
        rankedHolograms.clear();
        plugin.getLogger().info("Hologramas dos NPCs de rank removidos. Os NPCs serão salvos pelo Citizens.");
    }

    private void loadNpcs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        ConfigurationSection npcSection = locationsConfig.getConfigurationSection("npcs");
        if (npcSection == null) return;

        for (String key : npcSection.getKeys(false)) { // ex: "top1", "top2"
            try {
                int rank = Integer.parseInt(key.replace("top", ""));
                int npcId = npcSection.getInt(key + ".id", -1);
                Location loc = (Location) npcSection.get(key + ".location");

                if (loc == null) {
                    plugin.getLogger().warning("Localização para o NPC " + key + " não encontrada. Pulando.");
                    continue;
                }

                NPC npc = (npcId != -1) ? registry.getById(npcId) : null;

                if (npc == null) {
                    // Se o NPC não foi encontrado (deletado/corrompido), recria ele no local salvo.
                    plugin.getLogger().warning("NPC do Top " + rank + " (ID: " + npcId + ") não encontrado. Recriando...");

                    npc = registry.createNPC(EntityType.PLAYER, "§e#" + rank + " - Recriando...");
                    npc.spawn(loc);
                    npc.setProtected(true);

                    // Atualiza o arquivo de config com o NOVO ID do NPC recriado.
                    npcSection.set(key + ".id", npc.getId());
                    saveLocations();
                }

                // Garante que o NPC esteja spawnado no local correto.
                if (!npc.isSpawned() || !npc.getStoredLocation().getWorld().equals(loc.getWorld()) || npc.getStoredLocation().distanceSquared(loc) > 1) {
                    npc.teleport(loc, null);
                    if(!npc.isSpawned()) npc.spawn(loc);
                }

                // Adiciona o NPC (encontrado ou recriado) ao controle do plugin.
                rankedNpcs.put(rank, npc);

                // Remove hologramas antigos que possam ter ficado órfãos no mesmo local.
                HologramsAPI.getHolograms(plugin).stream()
                        .filter(h -> h.getLocation().getWorld().equals(loc.getWorld()) && h.getLocation().distanceSquared(loc.clone().add(0, 2.3, 0)) < 0.1)
                        .forEach(Hologram::delete);

                // Recria o holograma para ele.
                Location hologramLocation = loc.clone().add(0, 2.3, 0);
                Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
                rankedHolograms.put(rank, hologram);

            } catch (Exception e) {
                plugin.getLogger().severe("Falha ao carregar o NPC de rank: " + key);
                e.printStackTrace();
            }
        }

        if (!rankedNpcs.isEmpty()) {
            plugin.getLogger().info(rankedNpcs.size() + " NPCs de rank carregados.");
            Bukkit.getScheduler().runTaskLater(plugin, this::updateRankedNpcs, 40L); // Delay de 2s para skins e nomes.
        }
    }

    private void setupLocationsFile() {
        locationsFile = new File(plugin.getDataFolder(), "npclocations.yml");
        if (!locationsFile.exists()) {
            plugin.saveResource("npclocations.yml", false);
        }
        locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);
    }

    private void saveLocations() {
        try {
            locationsConfig.save(locationsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}