package com.vitorxp.SyncCordVX.gui;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MotivosGUI {

    public static Inventory getInventory(SyncCordVX plugin, String targetName, String punishmentType) {
        Inventory inv = Bukkit.createInventory(null, 54, "Motivos - " + targetName);

        List<String> motivos = plugin.getConfigManager().getReasons();

        for (int i = 0; i < motivos.size(); i++) {
            if (i >= 54) break;

            String motivo = motivos.get(i);
            ItemStack item = new ItemBuilder(Material.PAPER)
                    .setName("§e" + motivo)
                    .setLore("§7Clique para punir por:", "§f" + motivo, "§7Tipo: " + punishmentType)
                    .build();
            inv.setItem(i, item);
        }

        ItemStack customItem = new ItemBuilder(Material.BOOK_AND_QUILL)
                .setName("§aMotivo Customizado")
                .setLore("§7Clique para digitar um motivo", "§7personalizado")
                .build();
        inv.setItem(53, customItem);

        return inv;
    }
}