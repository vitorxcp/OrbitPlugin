package com.vitorxp.SyncCordVX.database;

import com.vitorxp.SyncCordVX.SyncCordVX;

import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private SyncCordVX plugin;
    private Connection connection;

    public DatabaseManager(SyncCordVX plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/punishments.db");
            createTables();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao inicializar database", e);
        }
    }

    private void createTables() {
        String createPunishmentsTable = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_name VARCHAR(32) NOT NULL," +
                "player_uuid VARCHAR(36)," +
                "ip VARCHAR(45)," +
                "staff_name VARCHAR(32) NOT NULL," +
                "staff_uuid VARCHAR(36) NOT NULL," +
                "reason TEXT NOT NULL," +
                "type TEXT CHECK(type IN ('BAN', 'BAN_IP', 'MUTE', 'KICK')) NOT NULL," +                "duration BIGINT," +
                "start_time BIGINT NOT NULL," +
                "active BOOLEAN DEFAULT TRUE," +
                "unpardon_time BIGINT," +
                "unpardon_staff VARCHAR(32)," +
                "unpardon_staff_uuid VARCHAR(36)" +
                ");";

        String createDiscordLinkTable = "CREATE TABLE IF NOT EXISTS discord_links (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "minecraft_uuid VARCHAR(36) NOT NULL UNIQUE," +
                "discord_id VARCHAR(20) NOT NULL UNIQUE," +
                "linked_at BIGINT NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPunishmentsTable);
            stmt.execute(createDiscordLinkTable);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar tabelas", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeDatabase();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao obter conexão", e);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao fechar conexão", e);
            }
        }
    }
}