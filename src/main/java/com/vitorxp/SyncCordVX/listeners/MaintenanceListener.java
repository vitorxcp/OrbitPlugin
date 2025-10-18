package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MaintenanceListener implements Listener {

    private final SyncCordVX plugin;

    public MaintenanceListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerPing(ServerListPingEvent event) {
        boolean emManutencao = plugin.getConfig().getBoolean("manutencao.ativo");

        if (emManutencao) {
            String linha1 = plugin.getConfig().getString("manutencao.motd.linha1", "§c§lSERVIDOR EM MANUTENÇÃO");
            String linha2 = plugin.getConfig().getString("manutencao.motd.linha2", "§7Voltamos em breve com novidades!");

            event.setMotd(linha1 + "\n" + linha2);

            event.setServerIcon(null);
            event.setMaxPlayers(0);
        }
    }
}