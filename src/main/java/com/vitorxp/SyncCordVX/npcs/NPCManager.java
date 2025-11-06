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
import java.util.ArrayList;
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
    private static final String NPC_METADATA_KEY = "SyncCordVX_EconomyNPC";

    public NPCManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupLocationsFile();
        loadNpcs();
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
        NPC npc = registry.createNPC(EntityType.PLAYER, rank + " - Carregando...");
        npc.spawn(location);
        npc.setProtected(true);
        npc.data().set(NPC_METADATA_KEY, true);

        String path = "npcs.top" + rank;
        locationsConfig.set(path + ".location", location);
        locationsConfig.set(path + ".id", npc.getId());
        saveLocations();

        rankedNpcs.put(rank, npc);
        Location hologramLocation = location.clone().add(0, 2.3, 0);
        Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
        rankedHolograms.put(rank, hologram);
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
                if (npc == null || hologram == null || !npc.isSpawned() || hologram.isDeleted()) continue;

                if (top3.size() >= i) {
                    Map.Entry<String, Double> entry = top3.get(i - 1);
                    String playerName = capitalize(entry.getKey());
                    double balance = entry.getValue();
                    String formattedBalance = String.format("%,.2f", balance);

                    npc.setName(playerName);
                    updateNpcSkin(npc, playerName);

                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i + " MONEY");
                    hologram.appendTextLine("§f" + playerName);
                    hologram.appendTextLine("§a$" + formattedBalance);
                } else {
                    npc.setName("Vazio");
                    updateNpcSkin(npc, "Steve");

                    hologram.clearLines();
                    hologram.appendTextLine("§6§lTOP #" + i + " MONEY");
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
        if (!rankedHolograms.isEmpty()) {
            rankedHolograms.values().forEach(h -> {
                if (h != null && !h.isDeleted()) h.delete();
            });
        }

        rankedNpcs.clear();
        rankedHolograms.clear();
    }

    private void loadNpcs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        new ArrayList<>(HologramsAPI.getHolograms(plugin)).forEach(Hologram::delete);
        rankedNpcs.clear();
        rankedHolograms.clear();

        ConfigurationSection npcSection = locationsConfig.getConfigurationSection("npcs");
        if (npcSection == null) return;

        for (String key : npcSection.getKeys(false)) {
            try {
                int rank = Integer.parseInt(key.replace("top", ""));
                Location loc = (Location) npcSection.get(key + ".location");
                if (loc == null) continue;

                int npcId = npcSection.getInt(key + ".id", -1);
                NPC npc = null;

                if (npcId != -1 && registry.getById(npcId) != null) {
                    npc = registry.getById(npcId);
                    if (!npc.isSpawned()) npc.spawn(loc);
                } else {
                    npc = registry.createNPC(EntityType.PLAYER, "§e#" + rank + " - Carregando...");
                    npc.spawn(loc);
                    npc.setProtected(true);
                    npc.data().set(NPC_METADATA_KEY, true);
                    npcSection.set(key + ".id", npc.getId());
                }

                rankedNpcs.put(rank, npc);

                Location hologramLocation = loc.clone().add(0, 2.3, 0);
                Hologram hologram = HologramsAPI.createHologram(plugin, hologramLocation);
                rankedHolograms.put(rank, hologram);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        saveLocations();

        if (!rankedNpcs.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::updateRankedNpcs, 60L);
        }
    }

    private void setupLocationsFile() {
        locationsFile = new File(plugin.getDataFolder(), "npclocations.yml");
        if (!locationsFile.exists()) {
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo npclocations.yml!");
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