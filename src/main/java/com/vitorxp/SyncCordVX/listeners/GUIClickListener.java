package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.gui.HistoricoGUI;
import com.vitorxp.SyncCordVX.gui.MotivosGUI;
import com.vitorxp.SyncCordVX.gui.PunicoesAtivasGUI;
import com.vitorxp.SyncCordVX.gui.PunirGUI;
import com.vitorxp.SyncCordVX.objects.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GUIClickListener implements Listener {

    private SyncCordVX plugin;

    public GUIClickListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();
        ItemStack item = event.getCurrentItem();

        if (item == null || !item.hasItemMeta()) return;


        if (title.startsWith("Punir ")) {
            event.setCancelled(true);
            handlePunirGUI(player, title, item);
            return;
        }

        if (title.startsWith("Motivos - ")) {
            event.setCancelled(true);
            handleMotivosGUI(player, title, item);
            return;
        }

        if (title.equals("Punições Ativas")) {
            event.setCancelled(true);
            handlePunicoesAtivasGUI(player, item);
            return;
        }
    }

    private void handlePunirGUI(Player player, String title, ItemStack item) {
        String targetName = title.substring(6);
        String itemName = item.getItemMeta().getDisplayName();

        if (itemName.contains("Banir") && !itemName.contains("IP")) {
            player.openInventory(MotivosGUI.getInventory(plugin, targetName, "BAN"));
        } else if (itemName.contains("Mutar")) {
            player.openInventory(MotivosGUI.getInventory(plugin, targetName, "MUTE"));
        } else if (itemName.contains("Kickar")) {
            player.openInventory(MotivosGUI.getInventory(plugin, targetName, "KICK"));
        } else if (itemName.contains("Banir IP")) {
            player.openInventory(MotivosGUI.getInventory(plugin, targetName, "BAN_IP"));
        }
    }

    private void handleMotivosGUI(Player player, String title, ItemStack item) {
        String targetName = title.substring(10);
        String itemName = item.getItemMeta().getDisplayName();
        String punishmentType = title.substring(title.lastIndexOf("-") + 2);

        if (itemName.equals("§aMotivo Customizado")) {
            player.closeInventory();
            player.sendMessage("§aDigite o motivo personalizado no chat:");
            return;
        }

        String reason = itemName.substring(2);
        applyPunishment(player, targetName, punishmentType, reason);
    }

    private void handlePunicoesAtivasGUI(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }

        for (String lore : item.getItemMeta().getLore()) {
            if (lore.startsWith("§7ID: ")) {
                try {
                    int punishmentId = Integer.parseInt(lore.substring(6));

                    plugin.getPunishmentManager().getAllActivePunishments().stream()
                            .filter(punishment -> punishment.getId() == punishmentId)
                            .findFirst()
                            .ifPresent(punishmentToRemove -> {

                                plugin.getPunishmentManager().removePunishment(punishmentToRemove, player.getName(), player.getUniqueId());

                                player.sendMessage("§aPunição removida com sucesso.");
                                player.closeInventory();

                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    player.openInventory(PunicoesAtivasGUI.getInventory(plugin));
                                }, 5L);
                            });

                    break;

                } catch (NumberFormatException e) {
                    player.sendMessage("§cOcorreu um erro ao ler o ID da punição no item.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void applyPunishment(Player player, String targetName, String punishmentType, String reason) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cJogador não encontrado.");
            return;
        }

        Punishment punishment = new Punishment();
        punishment.setPlayerName(targetName);
        punishment.setPlayerUUID(target.getUniqueId());
        punishment.setIp(target.getAddress().getAddress().getHostAddress());
        punishment.setStaffName(player.getName());
        punishment.setStaffUUID(player.getUniqueId());
        punishment.setReason(reason);
        punishment.setType(Punishment.Type.valueOf(punishmentType));
        punishment.setDuration(-1);
        punishment.setStartTime(System.currentTimeMillis());
        punishment.setActive(true);

        plugin.getPunishmentManager().punish(punishment);
        player.sendMessage("§aPunição aplicada com sucesso.");
        player.closeInventory();
    }
}