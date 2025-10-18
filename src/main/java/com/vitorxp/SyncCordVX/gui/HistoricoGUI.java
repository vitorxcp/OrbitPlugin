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

public class HistoricoGUI {

    public static Inventory getInventory(SyncCordVX plugin, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 54, "Histórico - " + targetName);

        List<Punishment> historico = plugin.getPunishmentManager().getPunishmentHistory(targetName, null);

        for (int i = 0; i < historico.size(); i++) {
            if (i >= 54) break;

            Punishment p = historico.get(i);
            ItemStack item = createPunishmentItem(p);
            inv.setItem(i, item);
        }

        return inv;
    }

    private static ItemStack createPunishmentItem(Punishment p) {
        Material material = getMaterialByType(p.getType());
        String status = p.isActive() ? "§aAtivo" : "§cExpirado/Removido";

        return new ItemBuilder(material)
                .setName("§e" + p.getType().name() + " - " + status)
                .setLore(
                        "§7Jogador: " + p.getPlayerName(),
                        "§7Motivo: " + p.getReason(),
                        "§7Staff: " + p.getStaffName(),
                        "§7Data: " + TimeUtil.formatTime(p.getStartTime()),
                        "§7Duração: " + (p.isPermanent() ? "Permanente" : TimeUtil.formatTime(p.getDuration())),
                        "§7Tipo: " + p.getType().name(),
                        p.getUnpardonStaff() != null ? "§7Removido por: " + p.getUnpardonStaff() : ""
                )
                .build();
    }

    private static Material getMaterialByType(Punishment.Type type) {
        switch (type) {
            case BAN: return Material.IRON_AXE;
            case BAN_IP: return Material.GOLD_AXE;
            case MUTE: return Material.PAPER;
            case KICK: return Material.LEATHER_BOOTS;
            default: return Material.PAPER;
        }
    }
}