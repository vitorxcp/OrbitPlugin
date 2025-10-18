package com.vitorxp.SyncCordVX.linking;

import com.vitorxp.SyncCordVX.SyncCordVX;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {

    private final SyncCordVX plugin;
    private final Map<UUID, String> verificationCodes = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingDiscordIds = new ConcurrentHashMap<>();

    public enum LinkStage {
        NONE,
        AWAITING_DISCORD_ID,
        AWAITING_CODE
    }
    private final Map<UUID, LinkStage> playerStages = new ConcurrentHashMap<>();

    public LinkManager(SyncCordVX plugin) {
        this.plugin = plugin;
    }

    public void startLinkageProcess(Player player) {
        player.closeInventory();
        player.sendMessage("§aPor favor, digite seu ID do Discord no chat.");
        player.sendMessage("§7(Exemplo: 123456789012345678)");
        setPlayerStage(player.getUniqueId(), LinkStage.AWAITING_DISCORD_ID);
        scheduleTimeout(player.getUniqueId(), 2 * 60);
    }

    public void receivedDiscordId(Player player, String discordId) {
        if (!discordId.matches("\\d{17,20}")) {
            player.sendMessage("§cID do Discord inválido. Certifique-se de que é apenas o número.");
            cancelLinkage(player.getUniqueId());
            return;
        }

        player.sendMessage("§eVerificando seu ID do Discord...");

        plugin.getLinkDAO().getMinecraftUUID(discordId).thenAccept(linkedUUID -> {

            if (linkedUUID != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cEsta conta do Discord já está vinculada a outra conta do Minecraft.");
                    cancelLinkage(player.getUniqueId());
                });
                return;
            }

            String code = generateVerificationCode();
            verificationCodes.put(player.getUniqueId(), code);
            pendingDiscordIds.put(player.getUniqueId(), discordId);

            plugin.getBotManager().sendVerificationMessage(discordId, code, success -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (success) {
                        player.sendMessage("§aEnviei um código de verificação no seu privado do Discord!");
                        player.sendMessage("§aDigite o código no chat para completar a vinculação.");
                        setPlayerStage(player.getUniqueId(), LinkStage.AWAITING_CODE);
                        scheduleTimeout(player.getUniqueId(), 5 * 60);
                    } else {
                        player.sendMessage("§cNão consegui enviar uma mensagem no seu privado do Discord!");
                        player.sendMessage("§cVerifique se nossas mensagens não estão bloqueadas e tente novamente com /vincular.");
                        cancelLinkage(player.getUniqueId());
                    }
                });
            });
        });
    }

    public void receivedVerificationCode(Player player, String code) {
        UUID uuid = player.getUniqueId();
        String correctCode = verificationCodes.get(uuid);

        if (correctCode != null && correctCode.equals(code)) {
            String discordId = pendingDiscordIds.get(uuid);
            plugin.getLinkDAO().linkAccount(uuid, discordId).thenRun(() -> {
                player.sendMessage("§a§lSUCESSO! §aSua conta do Minecraft foi vinculada ao Discord!");
            });
            cancelLinkage(uuid);
        } else {
            player.sendMessage("§cCódigo de verificação incorreto. Tente novamente.");
        }
    }

    public LinkStage getPlayerStage(UUID uuid) {
        return playerStages.getOrDefault(uuid, LinkStage.NONE);
    }

    public void setPlayerStage(UUID uuid, LinkStage stage) {
        if (stage == LinkStage.NONE) {
            playerStages.remove(uuid);
        } else {
            playerStages.put(uuid, stage);
        }
    }

    public void cancelLinkage(UUID uuid) {
        verificationCodes.remove(uuid);
        pendingDiscordIds.remove(uuid);
        playerStages.remove(uuid);
    }

    private String generateVerificationCode() {
        return String.format("%05d", new Random().nextInt(100000));
    }

    private void scheduleTimeout(UUID uuid, int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayerStage(uuid) != LinkStage.NONE) {
                    cancelLinkage(uuid);
                    Player player = plugin.getServer().getPlayer(uuid);
                    if(player != null && player.isOnline()){
                        player.sendMessage("§cO tempo para vinculação expirou.");
                    }
                }
            }
        }.runTaskLater(plugin, seconds * 20L);
    }
}