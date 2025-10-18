package com.vitorxp.SyncCordVX.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.entity.Player;

public class TabPacketListener extends PacketAdapter {

    public TabPacketListener(SyncCordVX plugin) {
        super(plugin, PacketType.Play.Client.TAB_COMPLETE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();

        String permission = "orbit.commands.tabcomplete";

        if (!player.hasPermission(permission)) {
            String buffer = event.getPacket().getStrings().read(0);

            if (buffer.startsWith("/") && !buffer.contains(" ")) {

                event.setCancelled(true);
            }
        }
    }
}