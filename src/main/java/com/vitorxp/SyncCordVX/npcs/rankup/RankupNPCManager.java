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
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RankupNPCManager {

    private final SyncCordVX plugin;
    private final Map<Integer, NPC> rankedNpcs = new HashMap<>();
    private final Map<Integer, Hologram> rankedHolograms = new HashMap<>();
    private FileConfiguration locationsConfig;
    private File locationsFile;
    private BukkitTask updateTask;
    private boolean isUpdating = false;
    private static final String NPC_METADATA_KEY = "SyncCordVX_RankupNPC";

    public RankupNPCManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupLocationsFile();
        loadNpcs();
    }

    private void setupLocationsFile() {
        locationsFile = new File(plugin.getDataFolder(), "rankup_npclocations.yml");
        if (!locationsFile.exists()) {
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo rankup_npclocations.yml!");
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

    public void setNpcLocation(int rank, Location location) {
        if (rankedNpcs.containsKey(rank)) {
            NPC oldNpc = rankedNpcs.remove(rank);
            if (oldNpc != null) oldNpc.destroy();
        }
        if (rankedHolograms.containsKey(rank)) {
            Hologram oldHologram = rankedHolograms.remove(rank);
            if (oldHologram != null && !oldHologram.isDeleted()) oldHologram.delete();
        }

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§e#Rank " + rank);
        npc.spawn(location);
        npc.setProtected(true);
        npc.data().set(NPC_METADATA_KEY, true);

        String path = "rankup_npcs.top" + rank;
        locationsConfig.set(path + ".location", location);
        locationsConfig.set(path + ".id", npc.getId());
        saveLocations();

        rankedNpcs.put(rank, npc);
        Location holoLoc = location.clone().add(0, 2.3, 0);
        Hologram hologram = HologramsAPI.createHologram(plugin, holoLoc);
        rankedHolograms.put(rank, hologram);

        triggerUpdate();
    }

    public void triggerUpdate() {
        if (isUpdating) return;
        isUpdating = true;
        if (updateTask != null) updateTask.cancel();
        updateTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, this::updateRankupNpcs);
    }

    private void updateRankupNpcs() {
        File dbFile = new File(plugin.getDataFolder().getParentFile(), "yRankup/database.db");
        if (!dbFile.exists()) {
            plugin.getLogger().warning("Arquivo database.db do yRankup não encontrado!");
            finishUpdate();
            return;
        }

        Map<String, Integer> playerRanks = new HashMap<>();
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT `key`, `json` FROM `yrankup.players`")) {

            while (rs.next()) {
                String player = rs.getString("key");
                String json = rs.getString("json");
                int rank = extractRankFromJson(json);
                if (rank > 0) playerRanks.put(player, rank);
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao ler database.db do yRankup: " + e.getMessage());
            finishUpdate();
            return;
        }

        if (playerRanks.isEmpty()) {
            plugin.getLogger().info("Nenhum dado de rank encontrado no banco de dados do yRankup.");
            finishUpdate();
            return;
        }

        List<Map.Entry<String, Integer>> top3 = playerRanks.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());

        Bukkit.getScheduler().runTask(plugin, () -> updateVisualTop(top3));
    }

    private void updateVisualTop(List<Map.Entry<String, Integer>> top3) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int i = 1; i <= 3; i++) {
                NPC npc = rankedNpcs.get(i);
                Hologram hologram = rankedHolograms.get(i);

                if (npc == null || hologram == null) continue;
                if (hologram.isDeleted()) {
                    Location loc = npc.getStoredLocation().clone().add(0, 2.3, 0);
                    hologram = HologramsAPI.createHologram(plugin, loc);
                    rankedHolograms.put(i, hologram);
                }

                try {
                    hologram.clearLines();
                } catch (Exception e) {
                    plugin.getLogger().warning("Falha ao limpar holograma do TOP " + i + ": " + e.getMessage());
                    continue;
                }

                if (top3.size() >= i) {
                    Map.Entry<String, Integer> entry = top3.get(i - 1);
                    String name = capitalize(entry.getKey());
                    int rankValue = entry.getValue();

                    String rankName = plugin.getConfig().getString("rankup-npcs.rank-names." + rankValue, "Rank " + rankValue);
                    npc.setName(name);
                    updateNpcSkin(npc, name);

                    hologram.appendTextLine("§6§lTOP #" + i + " RANK");
                    hologram.appendTextLine("§f" + name);
                    hologram.appendTextLine("§b" + rankName);
                } else {
                    npc.setName("§7#" + i + " - Vazio");
                    updateNpcSkin(npc, "Steve");
                    hologram.appendTextLine("§6§lTOP #" + i + " RANK");
                    hologram.appendTextLine("§7- Vazio -");
                }
            }
            finishUpdate();
        });
    }


    private void finishUpdate() {
        isUpdating = false;
    }

    private int extractRankFromJson(String json) {
        if (json == null) return -1;
        String search = "\"rank\":";
        int index = json.indexOf(search);
        if (index == -1) return -1;
        String after = json.substring(index + search.length()).trim();
        int end = after.indexOf(",");
        if (end == -1) end = after.indexOf("}");
        try {
            return Integer.parseInt(after.substring(0, end).replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    private void updateNpcSkin(NPC npc, String skinName) {
        String current = npc.data().get("player-skin-name");
        if (current != null && current.equalsIgnoreCase(skinName)) return;
        npc.data().set("player-skin-name", skinName);
        if (npc.isSpawned()) {
            Location loc = npc.getStoredLocation();
            npc.despawn();
            npc.spawn(loc);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public void shutdown() {
        if (updateTask != null) updateTask.cancel();
        rankedHolograms.values().forEach(h -> {
            if (h != null && !h.isDeleted()) h.delete();
        });
        rankedNpcs.values().forEach(npc -> {
            if (npc != null && npc.isSpawned()) npc.despawn();
        });
        rankedHolograms.clear();
        rankedNpcs.clear();
    }

    private void loadNpcs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        rankedNpcs.clear();
        rankedHolograms.clear();

        ConfigurationSection section = locationsConfig.getConfigurationSection("rankup_npcs");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                int rank = Integer.parseInt(key.replace("top", ""));
                Location loc = (Location) section.get(key + ".location");
                if (loc == null) continue;

                int id = section.getInt(key + ".id", -1);
                NPC npc = (id != -1) ? registry.getById(id) : null;

                if (npc == null) {
                    npc = registry.createNPC(EntityType.PLAYER, "§e#" + rank + " - Carregando...");
                    npc.spawn(loc);
                    npc.setProtected(true);
                    npc.data().set(NPC_METADATA_KEY, true);
                    section.set(key + ".id", npc.getId());
                } else if (!npc.isSpawned()) {
                    npc.spawn(loc);
                }

                rankedNpcs.put(rank, npc);

                Location holoLoc = loc.clone().add(0, 2.3, 0);
                Hologram hologram = HologramsAPI.createHologram(plugin, holoLoc);
                hologram.appendTextLine("§7Carregando...");
                rankedHolograms.put(rank, hologram);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        saveLocations();
        Bukkit.getScheduler().runTaskLater(plugin, this::triggerUpdate, 60L);
    }
}