package com.vitorxp.SyncCordVX.gui;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import com.vitorxp.SyncCordVX.utils.ItemBuilder;
import com.vitorxp.SyncCordVX.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PunicoesAtivasGUI {

    public static Inventory getInventory(SyncCordVX plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, "Punições Ativas");

        List<Punishment> punicoesAtivas = plugin.getPunishmentManager().getAllActivePunishments();

        for (int i = 0; i < punicoesAtivas.size(); i++) {
            if (i >= 54) break;

            Punishment p = punicoesAtivas.get(i);
            ItemStack item = createPunishmentItem(p);
            inv.setItem(i, item);
        }

        return inv;
    }

    private static ItemStack createPunishmentItem(Punishment p) {
        Material material = getMaterialByType(p.getType());
        String timeLeft = p.isPermanent() ? "Permanente" :
                TimeUtil.formatTime(p.getEndTime() - System.currentTimeMillis());

        return new ItemBuilder(material)
                .setName("§e" + p.getType().name() + " - " + p.getPlayerName())
                .setLore(
                        "§7Motivo: " + p.getReason(),
                        "§7Staff: " + p.getStaffName(),
                        "§7Data: " + TimeUtil.formatTime(p.getStartTime()),
                        "§7Tempo restante: " + timeLeft,
                        "§7Tipo: " + p.getType().name(),
                        "§7ID: " + p.getId(),
                        "",
                        "§cClique para remover esta punição"
                )
                .build();
    }

    private static Material getMaterialByType(Punishment.Type type) {
        switch (type) {
            case BAN: return Material.BARRIER;
            case BAN_IP: return Material.OBSIDIAN;
            case MUTE: return Material.WEB;
            case KICK: return Material.STICK;
            default: return Material.PAPER;
        }
    }
}