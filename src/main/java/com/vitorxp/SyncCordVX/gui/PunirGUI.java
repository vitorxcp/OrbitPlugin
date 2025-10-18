package com.vitorxp.SyncCordVX.gui;

import com.vitorxp.SyncCordVX.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PunirGUI {

    public static Inventory getInventory(String targetName) {
        Inventory inv = Bukkit.createInventory(null, 27, "Punir " + targetName);

        ItemStack banItem = new ItemBuilder(Material.IRON_AXE)
                .setName("§cBanir")
                .setLore("§7Clique para banir o jogador", "§7" + targetName)
                .build();

        ItemStack muteItem = new ItemBuilder(Material.PAPER)
                .setName("§eMutar")
                .setLore("§7Clique para mutar o jogador", "§7" + targetName)
                .build();

        ItemStack kickItem = new ItemBuilder(Material.LEATHER_BOOTS)
                .setName("§6Kickar")
                .setLore("§7Clique para kickar o jogador", "§7" + targetName)
                .build();

        ItemStack banIPItem = new ItemBuilder(Material.GOLD_AXE)
                .setName("§4Banir IP")
                .setLore("§7Clique para banir o IP do jogador", "§7" + targetName)
                .build();

        ItemStack customItem = new ItemBuilder(Material.BOOK)
                .setName("§aPunição Customizada")
                .setLore("§7Clique para punição customizada", "§7" + targetName)
                .build();

        inv.setItem(11, banItem);
        inv.setItem(12, muteItem);
        inv.setItem(13, kickItem);
        inv.setItem(14, banIPItem);
        inv.setItem(15, customItem);

        return inv;
    }
}