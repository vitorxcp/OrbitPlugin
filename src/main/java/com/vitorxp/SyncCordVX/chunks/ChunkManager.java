package com.vitorxp.SyncCordVX.chunks;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkManager {

    private final SyncCordVX plugin;
    private final Set<Location> keptChunks = new HashSet<>();
    private File configFile;
    private FileConfiguration config;

    public ChunkManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupConfigFile();
        loadChunks();
    }

    public boolean addChunk(Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX() * 16, 64, chunk.getZ() * 16);
        if (keptChunks.add(loc)) {
            saveChunks();
            return true;
        }
        return false;
    }

    public boolean removeChunk(Chunk chunk) {
        Location loc = new Location(chunk.getWorld(), chunk.getX() * 16, 64, chunk.getZ() * 16);
        if (keptChunks.remove(loc)) {
            saveChunks();
            return true;
        }
        return false;
    }

    public Set<Location> getKeptChunks() {
        return keptChunks;
    }

    private void loadChunks() {
        List<Location> locations = (List<Location>) config.getList("kept-chunks");
        if (locations != null) {
            keptChunks.addAll(locations);
        }
        plugin.getLogger().info(keptChunks.size() + " chunks estão sendo mantidos carregados.");
    }

    private void saveChunks() {
        config.set("kept-chunks", new java.util.ArrayList<>(keptChunks));
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupConfigFile() {
        configFile = new File(plugin.getDataFolder(), "kept-chunks.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}