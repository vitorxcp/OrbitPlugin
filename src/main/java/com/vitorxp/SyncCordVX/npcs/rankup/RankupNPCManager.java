package com.vitorxp.SyncCordVX.npcs.rankup;

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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankupNPCManager {

    private final SyncCordVX plugin;
    private final Map<Integer, NPC> rankedNpcs = new HashMap<>();
    private final Map<Integer, Hologram> rankedHolograms = new HashMap<>();
    private FileConfiguration locationsConfig;
    private File locationsFile;

    public RankupNPCManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupLocationsFile();
        loadNpcs();
    }

    public void setNpcLocation(int rank, Location location) {
        if (rankedNpcs.containsKey(rank)) rankedNpcs.get(rank).destroy();
        if (rankedHolograms.containsKey(rank)) rankedHolograms.get(rank).delete();

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§e#Rank " + rank);
        npc.spawn(location);
        npc.setProtected(true);

        String path = "rankup_npcs.top" + rank;
        locationsConfig.set(path + ".location", location);
        locationsConfig.set(path + ".id", npc.getId());
        saveLocations();

        rankedNpcs.put(rank, npc);

        Location hologramLocation = location.clone().add(0, 2.3, 0);
        Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
        hologram.appendTextLine("§e§lTOP #" + rank + " Rank");
        rankedHolograms.put(rank, hologram);
        updateRankupNpcs();
    }

    public void updateRankupNpcs() {
        // Lógica de leitura do banco de dados SQLite
        Map<String, Integer> playerRanks = new HashMap<>();
        File dbFile = new File(plugin.getDataFolder().getParentFile(), "yRankup/database.db");
        if (!dbFile.exists()) {
            plugin.getLogger().warning("Arquivo database.db do yRankup não encontrado!");
            return;
        }

        String url = "jdbc:sqlite:" + dbFile.getPath();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT `key`, `json` FROM `yrankup.players`")) {

            while (rs.next()) {
                String playerName = rs.getString("key");
                String json = rs.getString("json");
                // Extração simples do rank do JSON, sem bibliotecas externas
                int rank = extractRankFromJson(json);
                if (rank > 0) {
                    playerRanks.put(playerName, rank);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Ordena os jogadores pelo maior rank
        List<Map.Entry<String, Integer>> top3 = playerRanks.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());

        // Atualiza os NPCs na thread principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int i = 1; i <= 3; i++) {
                NPC npc = rankedNpcs.get(i);
                Hologram hologram = rankedHolograms.get(i);
                if (npc == null || hologram == null) continue;

                if (top3.size() >= i) {
                    Map.Entry<String, Integer> entry = top3.get(i - 1);
                    String playerName = capitalize(entry.getKey());
                    int playerRank = entry.getValue();

                    // Pega o nome do rank customizado do config.yml
                    String rankName = plugin.getConfig().getString("rankup-npcs.rank-names." + playerRank, "Rank " + playerRank);

                    npc.setName("§e#" + i + " - " + playerName);
                    updateNpcSkin(npc, playerName);

                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i + " RANK");
                    hologram.appendTextLine("§f" + playerName);
                    hologram.appendTextLine("§b" + rankName);
                } else {
                    npc.setName("§7#" + i + " - Vazio");
                    updateNpcSkin(npc, "Steve");
                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i + " RANK");
                    hologram.appendTextLine("§7- Vazio -");
                }
            }
        });
    }

    // Método auxiliar para extrair o rank do JSON
    private int extractRankFromJson(String json) {
        String searchKey = "\"rank\":";
        int index = json.indexOf(searchKey);
        if (index == -1) return -1;

        String fromRank = json.substring(index + searchKey.length());
        int endIndex = fromRank.indexOf(",");
        if(endIndex == -1) endIndex = fromRank.indexOf("}");

        try {
            return Integer.parseInt(fromRank.substring(0, endIndex));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateNpcSkin(NPC npc, String skinName) {
        String currentSkin = npc.data().get("player-skin-name");
        if (currentSkin != null && currentSkin.equalsIgnoreCase(skinName)) return;
        npc.data().set("player-skin-name", skinName);
        if (npc.isSpawned()) {
            Location loc = npc.getStoredLocation();
            npc.despawn();
            npc.spawn(loc);
        }
    }

    public void shutdown() {
        if (!rankedHolograms.isEmpty()) rankedHolograms.values().forEach(Hologram::delete);
        rankedNpcs.clear();
        rankedHolograms.clear();
    }

    private void loadNpcs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        ConfigurationSection npcSection = locationsConfig.getConfigurationSection("rankup_npcs");
        if (npcSection == null) return;
        for (String key : npcSection.getKeys(false)) {
            try {
                int rank = Integer.parseInt(key.replace("top", ""));
                int npcId = npcSection.getInt(key + ".id", -1);
                Location loc = (Location) npcSection.get(key + ".location");

                if (loc == null) continue;

                NPC npc = (npcId != -1) ? registry.getById(npcId) : null;
                if (npc == null) {
                    npc = registry.createNPC(EntityType.PLAYER, "§e#" + rank + " - Recriando...");
                    npc.spawn(loc);
                    npc.setProtected(true);
                    npcSection.set(key + ".id", npc.getId());
                    saveLocations();
                }

                if (!npc.isSpawned() || !npc.getStoredLocation().equals(loc)) {
                    npc.teleport(loc, null);
                    if(!npc.isSpawned()) npc.spawn(loc);
                }

                rankedNpcs.put(rank, npc);

                HologramsAPI.getHolograms(plugin).stream()
                        .filter(h -> h.getLocation().distanceSquared(loc.clone().add(0, 2.3, 0)) < 0.1)
                        .forEach(Hologram::delete);

                Location hologramLocation = loc.clone().add(0, 2.3, 0);
                Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
                rankedHolograms.put(rank, hologram);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!rankedNpcs.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::updateRankupNpcs, 40L);
        }
    }

    private void setupLocationsFile() {
        locationsFile = new File(plugin.getDataFolder(), "rankup_npclocations.yml");
        if (!locationsFile.exists()) {
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
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