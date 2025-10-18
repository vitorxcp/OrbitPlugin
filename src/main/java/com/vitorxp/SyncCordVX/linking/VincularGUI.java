package com.vitorxp.SyncCordVX.linking;

import com.vitorxp.SyncCordVX.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class VincularGUI {

    public static Inventory getInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Vincular Contas");

        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta playerMeta = (SkullMeta) playerHead.getItemMeta();
        playerMeta.setOwner(player.getName());
        playerMeta.setDisplayName("§aVincular sua Conta");
        playerMeta.setLore(Arrays.asList(
                "§7Clique aqui para iniciar o processo",
                "§7de vinculação da sua conta do Minecraft",
                "§7com a sua conta do Discord.",
                "",
                "§e► Clique para começar!"
        ));
        playerHead.setItemMeta(playerMeta);

        ItemStack discordHead = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJjYTI3Y2FiODc3MjI4OTZkYzY2OGM3YjliNzZlNmZjM2UyZGNlNzcwNTgzYWRmYmUxNjJhZGM5NGU5ZDgyZCJ9fX0=")
                .setName("§9Nosso Discord")
                .setLore(Arrays.asList(
                        "§7Ainda não está em nosso servidor do Discord?",
                        "",
                        "§fLink: §bhttps://discord.gg/DCjbGkcyMb",
                        "",
                        "§e► Clique para receber o link!"
                )).build();


        inv.setItem(11, playerHead);
        inv.setItem(15, discordHead);

        return inv;
    }
}