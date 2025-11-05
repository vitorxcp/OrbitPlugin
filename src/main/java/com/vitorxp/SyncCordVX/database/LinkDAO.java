package com.vitorxp.SyncCordVX.database;

import com.vitorxp.SyncCordVX.SyncCordVX;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LinkDAO {

    private final SyncCordVX plugin;
    private final DatabaseManager databaseManager;

    public LinkDAO(SyncCordVX plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        createTable();
    }

    private void createTable() {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_links (" +
                             "minecraft_uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                             "discord_id VARCHAR(30) NOT NULL" +
                             ");"
             )) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar a tabela 'player_links': " + e.getMessage());
        }
    }

    public CompletableFuture<Void> linkAccount(UUID minecraftUUID, String discordId) {
        return CompletableFuture.runAsync(() -> {
            String query = "REPLACE INTO player_links (minecraft_uuid, discord_id) VALUES (?, ?);";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, minecraftUUID.toString());
                ps.setString(2, discordId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao vincular conta: " + e.getMessage());
            }
        });
    }

    public void getLinkedDiscordId(UUID minecraftUUID, Consumer<String> callback) {
        getDiscordId(minecraftUUID).thenAccept(callback);
    }

    public CompletableFuture<String> getDiscordId(UUID minecraftUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT discord_id FROM player_links WHERE minecraft_uuid = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, minecraftUUID.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("discord_id");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao buscar discord_id: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> unlinkAccount(UUID minecraftUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "DELETE FROM player_links WHERE minecraft_uuid = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, minecraftUUID.toString());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao desvincular conta: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<UUID> getMinecraftUUID(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT minecraft_uuid FROM player_links WHERE discord_id = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, discordId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return UUID.fromString(rs.getString("minecraft_uuid"));
                }
            } catch (SQLException | IllegalArgumentException e) {
                plugin.getLogger().severe("Erro ao buscar minecraft_uuid por discord_id: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> isLinked(UUID minecraftUUID) {
        return getDiscordId(minecraftUUID).thenApply(discordId -> discordId != null);
    }
}