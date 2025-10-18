package com.vitorxp.SyncCordVX.pads;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SlimePadManager {

    private final SyncCordVX plugin;
    private final Set<Location> slimePads = new HashSet<>();
    private FileConfiguration padsConfig;
    private File padsFile;

    public SlimePadManager(SyncCordVX plugin) {
        this.plugin = plugin;
        setupPadsFile();
        loadPads();
    }


    public void addSlimePad(Location location) {
        // Normaliza a localização para ignorar a direção que o jogador olha
        Location blockLocation = location.getBlock().getLocation();
        if (slimePads.add(blockLocation)) {
            savePads();
        }
    }

    public void removeSlimePad(Location location) {
        Location blockLocation = location.getBlock().getLocation();
        if (slimePads.remove(blockLocation)) {
            savePads();
        }
    }

    public boolean isSlimePad(Location location) {
        return slimePads.contains(location.getBlock().getLocation());
    }

    public void loadPads() { // <-- Mude de private para public
        slimePads.clear(); // Limpa a lista antiga antes de carregar a nova
        List<?> locationList = padsConfig.getList("pads");
        if (locationList == null) return;

        for (Object obj : locationList) {
            if (obj instanceof Location) {
                slimePads.add((Location) obj);
            }
        }
        plugin.getLogger().info(slimePads.size() + " slime pads recarregados!");
    }

    public Set<Location> getSlimePads() {
        return slimePads;
    }

    private void savePads() {
        padsConfig.set("pads", new java.util.ArrayList<>(slimePads));
        try {
            padsConfig.save(padsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupPadsFile() {
        padsFile = new File(plugin.getDataFolder(), "slimepads.yml");
        if (!padsFile.exists()) {
            try {
                padsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        padsConfig = YamlConfiguration.loadConfiguration(padsFile);
    }
}