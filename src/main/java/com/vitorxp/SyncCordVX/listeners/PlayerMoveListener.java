package com.vitorxp.SyncCordVX.listeners;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.pads.SlimePadManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final SyncCordVX plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final double PLAYER_BOUNDING_BOX_RADIUS = 0.3; // Metade da largura do jogador

    public PlayerMoveListener(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Otimização: só roda se o jogador mudar de bloco
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.isFlying() || isInCooldown(player)) {
            return;
        }

        SlimePadManager manager = plugin.getSlimePadManager();
        if (manager == null) return;

        // --- LÓGICA DEFINITIVA: VERIFICAÇÃO DA BOUNDING BOX DO JOGADOR ---

        // Pega a localização para onde o jogador está se movendo
        Location to = event.getTo();

        // Cria um conjunto para armazenar os blocos únicos sob os pés do jogador
        Set<Block> blocksBelow = new HashSet<>();

        // Adiciona os blocos sob os quatro cantos da "caixa" de colisão do jogador
        blocksBelow.add(to.clone().add(PLAYER_BOUNDING_BOX_RADIUS, -0.1, PLAYER_BOUNDING_BOX_RADIUS).getBlock());
        blocksBelow.add(to.clone().add(-PLAYER_BOUNDING_BOX_RADIUS, -0.1, PLAYER_BOUNDING_BOX_RADIUS).getBlock());
        blocksBelow.add(to.clone().add(PLAYER_BOUNDING_BOX_RADIUS, -0.1, -PLAYER_BOUNDING_BOX_RADIUS).getBlock());
        blocksBelow.add(to.clone().add(-PLAYER_BOUNDING_BOX_RADIUS, -0.1, -PLAYER_BOUNDING_BOX_RADIUS).getBlock());

        // Itera pelos blocos encontrados
        for (Block block : blocksBelow) {
            if (block.getType() == Material.SLIME_BLOCK && manager.isSlimePad(block.getLocation())) {
                launchPlayer(player);
                setCooldown(player, 1000); // Cooldown de 1 segundo
                return; // Para a execução para não lançar múltiplas vezes
            }
        }
        // --- FIM DA LÓGICA ---
    }

    private void launchPlayer(Player player) {
        Vector velocity = player.getVelocity();
        // Mantém a velocidade horizontal, mas zera a vertical para um pulo consistente
        velocity.setY(0);

        // Aplica a força do pulo
        player.setVelocity(velocity.add(new Vector(0, 2.2, 0)));

        player.playSound(player.getLocation(), Sound.SLIME_WALK2, 1.5F, 1.0F);
    }

    private boolean isInCooldown(Player player) {
        return cooldowns.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();
    }

    private void setCooldown(Player player, long milliseconds) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + milliseconds);
    }
}