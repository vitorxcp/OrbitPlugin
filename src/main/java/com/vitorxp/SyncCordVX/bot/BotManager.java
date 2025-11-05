package com.vitorxp.SyncCordVX.bot;

import com.vitorxp.SyncCordVX.SyncCordVX;
import com.vitorxp.SyncCordVX.objects.Punishment;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.configuration.ConfigurationSection;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class BotManager {

    private final SyncCordVX plugin;
    private JDA jda;
    private boolean enabled;

    public BotManager(SyncCordVX plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("discord.enabled", false);

        if (enabled) {
            initializeBot();
        }
    }

    private void initializeBot() {
        String token = plugin.getConfig().getString("discord.token");
        if (token == null || token.equals("SEU_TOKEN_AQUI")) {
            plugin.getLogger().warning("Token do Discord não configurado! Configure em config.yml");
            enabled = false;
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .build();

            jda.awaitReady();
            plugin.getLogger().info("Bot do Discord conectado com sucesso!");

        } catch (LoginException | InterruptedException | IllegalArgumentException e) {
            plugin.getLogger().severe("Erro ao conectar bot do Discord: " + e.getMessage());
            enabled = false;

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendPunishmentEmbed(Punishment punishment) {
        if (!enabled || jda == null) {
            plugin.getLogger().warning("Bot do Discord não está habilitado ou não conectado.");
            return;
        }

        String channelId = plugin.getConfig().getString("discord.channel");
        if (channelId == null || channelId.isEmpty()) {
            plugin.getLogger().warning("Canal do Discord não configurado!");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                plugin.getLogger().warning("Canal do Discord não encontrado: " + channelId);
                return;
            }

            ConfigurationSection embedConfig = plugin.getConfigManager().getDiscordEmbedAddPunishment();
            if (embedConfig == null) {
                plugin.getLogger().warning("Configuração de embed não encontrada!");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            String title = embedConfig.getString("title", "Punição Aplicada");
            if (title != null && !title.isEmpty()) {
                embed.setTitle(replacePlaceholders(title, punishment));
            }

            String description = embedConfig.getString("description", "");
            if (description != null && !description.isEmpty()) {
                embed.setDescription(replacePlaceholders(description, punishment));
            }

            String colorHex = embedConfig.getString("color", "#FF0000");
            try {
                embed.setColor(Color.decode(colorHex));
            } catch (Exception e) {
                embed.setColor(Color.RED);
            }

            if (embedConfig.contains("thumbnail")) {
                String thumbnail = embedConfig.getString("thumbnail");
                if (thumbnail != null && !thumbnail.isEmpty()) {
                    embed.setThumbnail(thumbnail);
                }
            }

            if (embedConfig.contains("footer")) {
                String footer = embedConfig.getString("footer");
                if (footer != null && !footer.isEmpty()) {
                    embed.setFooter(replacePlaceholders(footer, punishment), null);
                }
            }

            if (embedConfig.contains("fields")) {
                ConfigurationSection fieldsSection = embedConfig.getConfigurationSection("fields");
                if (fieldsSection != null) {
                    for (String fieldKey : fieldsSection.getKeys(false)) {
                        ConfigurationSection fieldConfig = fieldsSection.getConfigurationSection(fieldKey);
                        if (fieldConfig != null) {
                            String name = replacePlaceholders(fieldConfig.getString("name", ""), punishment);
                            String value = replacePlaceholders(fieldConfig.getString("value", ""), punishment);
                            boolean inline = fieldConfig.getBoolean("inline", false);

                            if (!name.isEmpty() && !value.isEmpty()) {
                                embed.addField(name, value, inline);
                            }
                        }
                    }
                }
            }

            if (embedConfig.getBoolean("timestamp", true)) {
                embed.setTimestamp(Instant.now());
            }

            channel.sendMessage(embed.build()).queue(
                    success -> plugin.getLogger().info("Embed de punição enviada para o Discord com sucesso!"),
                    error -> plugin.getLogger().warning("Erro ao enviar embed para Discord: " + error.getMessage())
            );

        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao enviar embed para Discord: " + e.getMessage());
        }
    }

    public void sendPunishmentRemoveEmbed(Punishment punishment) {
        if (!enabled || jda == null) {
            plugin.getLogger().warning("Bot do Discord não está habilitado ou não conectado.");
            return;
        }

        String channelId = plugin.getConfig().getString("discord.channel");
        if (channelId == null || channelId.isEmpty()) {
            plugin.getLogger().warning("Canal do Discord não configurado!");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                plugin.getLogger().warning("Canal do Discord não encontrado: " + channelId);
                return;
            }

            ConfigurationSection embedConfig = plugin.getConfigManager().getDiscordEmbedRemovePunishment();
            if (embedConfig == null) {
                plugin.getLogger().warning("Configuração de embed não encontrada!");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();

            String title = embedConfig.getString("title", "Punição Removida");
            if (title != null && !title.isEmpty()) {
                embed.setTitle(replacePlaceholders(title, punishment));
            }

            String description = embedConfig.getString("description", "");
            if (description != null && !description.isEmpty()) {
                embed.setDescription(replacePlaceholders(description, punishment));
            }

            String colorHex = embedConfig.getString("color", "#39B906");
            try {
                embed.setColor(Color.decode(colorHex));
            } catch (Exception e) {
                embed.setColor(Color.RED);
            }

            if (embedConfig.contains("thumbnail")) {
                String thumbnail = embedConfig.getString("thumbnail");
                if (thumbnail != null && !thumbnail.isEmpty()) {
                    embed.setThumbnail(thumbnail);
                }
            }

            if (embedConfig.contains("footer")) {
                String footer = embedConfig.getString("footer");
                if (footer != null && !footer.isEmpty()) {
                    embed.setFooter(replacePlaceholders(footer, punishment), null);
                }
            }

            if (embedConfig.contains("fields")) {
                ConfigurationSection fieldsSection = embedConfig.getConfigurationSection("fields");
                if (fieldsSection != null) {
                    for (String fieldKey : fieldsSection.getKeys(false)) {
                        ConfigurationSection fieldConfig = fieldsSection.getConfigurationSection(fieldKey);
                        if (fieldConfig != null) {
                            String name = replacePlaceholders(fieldConfig.getString("name", ""), punishment);
                            String value = replacePlaceholders(fieldConfig.getString("value", ""), punishment);
                            boolean inline = fieldConfig.getBoolean("inline", false);

                            if (!name.isEmpty() && !value.isEmpty()) {
                                embed.addField(name, value, inline);
                            }
                        }
                    }
                }
            }

            if (embedConfig.getBoolean("timestamp", true)) {
                embed.setTimestamp(Instant.now());
            }

            channel.sendMessage(embed.build()).queue(
                    success -> plugin.getLogger().info("Embed de punição enviada para o Discord com sucesso!"),
                    error -> plugin.getLogger().warning("Erro ao enviar embed para Discord: " + error.getMessage())
            );

        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao enviar embed para Discord: " + e.getMessage());
        }
    }

    public void sendVerificationMessage(String userId, String code, Consumer<Boolean> callback) {
        if (!enabled || jda == null) {
            callback.accept(false);
            return;
        }

        try {
            jda.retrieveUserById(userId).queue(user -> {
                user.openPrivateChannel().queue(privateChannel -> {
                    String message = "Olá! Seu código para vincular sua conta no servidor de Minecraft é: **" + code + "**\n" +
                            "Este código expira em 5 minutos.";

                    privateChannel.sendMessage(message).queue(
                            success -> callback.accept(true),
                            error -> {
                                if (error instanceof ErrorResponseException && ((ErrorResponseException) error).getErrorCode() == 50007) {
                                    plugin.getLogger().warning("Não foi possível enviar DM para " + user.getAsTag() + ". DMs bloqueadas.");
                                } else {
                                    plugin.getLogger().warning("Erro ao enviar DM: " + error.getMessage());
                                }
                                callback.accept(false);
                            }
                    );
                }, error -> {
                    plugin.getLogger().warning("Não foi possível abrir canal privado com " + user.getAsTag() + ".");
                    callback.accept(false);
                });
            }, error -> {
                plugin.getLogger().warning("Usuário do Discord com ID " + userId + " não encontrado.");
                callback.accept(false);
            });
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("ID do Discord fornecido não é um número válido: " + userId);
            callback.accept(false);
        }
    }

    public void retrieveDiscordUser(String userId, Consumer<User> callback, Consumer<Throwable> failureCallback) {
        if (!enabled || jda == null) {
            failureCallback.accept(new IllegalStateException("O Bot do Discord não está habilitado."));
            return;
        }
        jda.retrieveUserById(userId).queue((Consumer<? super User>) callback, failureCallback);
    }

    private String replacePlaceholders(String text, Punishment punishment) {
        if (text == null) return "";

        return text
                .replace("%player%", punishment.getPlayerName())
                .replace("%staff%", punishment.getStaffName())
                .replace("%reason%", punishment.getReason())
                .replace("%type%", getTypeDisplayName(punishment.getType()))
                .replace("%time%", formatDuration(punishment.getDuration()))
                .replace("%date%", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                        .format(java.time.LocalDateTime.now()))
                .replace("%id%", String.valueOf(punishment.getId()));
    }

    private String getTypeDisplayName(Punishment.Type type) {
        switch (type) {
            case BAN: return "Banimento";
            case BAN_IP: return "Banimento de IP";
            case MUTE: return "Mute";
            case KICK: return "Kick";
            default: return type.name();
        }
    }

    private String formatDuration(long duration) {
        if (duration == -1) {
            return "Permanente";
        }
        return com.vitorxp.SyncCordVX.utils.TimeUtil.formatTime(duration);
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            jda.shutdownNow();
            plugin.getLogger().info("Conexão com o Discord (JDA) foi finalizada.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public JDA getJDA() {
        return jda;
    }
}