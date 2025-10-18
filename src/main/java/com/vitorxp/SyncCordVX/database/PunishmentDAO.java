package com.vitorxp.SyncCordVX.database;

import com.vitorxp.SyncCordVX.objects.Punishment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentDAO {

    private DatabaseManager databaseManager;

    public PunishmentDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addPunishment(Punishment punishment) {
        String sql = "INSERT INTO punishments (player_name, player_uuid, ip, staff_name, staff_uuid, reason, type, duration, start_time, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, punishment.getPlayerName());
            pstmt.setString(2, punishment.getPlayerUUID() != null ? punishment.getPlayerUUID().toString() : null);
            pstmt.setString(3, punishment.getIp());
            pstmt.setString(4, punishment.getStaffName());
            pstmt.setString(5, punishment.getStaffUUID().toString());
            pstmt.setString(6, punishment.getReason());
            pstmt.setString(7, punishment.getType().name());
            pstmt.setLong(8, punishment.getDuration());
            pstmt.setLong(9, punishment.getStartTime());
            pstmt.setBoolean(10, punishment.isActive());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deactivatePunishment(int punishmentId) {
        String sql = "UPDATE punishments SET active = 0 WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, punishmentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePunishment(int punishmentId, String staffName, UUID staffUUID, long unpardonTime) {
        String sql = "UPDATE punishments SET active = 0, unpardon_staff = ?, unpardon_staff_uuid = ?, unpardon_time = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staffName);
            pstmt.setString(2, staffUUID.toString());
            pstmt.setLong(3, unpardonTime);
            pstmt.setInt(4, punishmentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Punishment> getActivePunishmentsByPlayer(String playerName, UUID playerUUID) {
        String sql = "SELECT * FROM punishments WHERE (player_uuid = ? OR player_name = ?) AND active = 1";
        List<Punishment> punishments = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID != null ? playerUUID.toString() : null);
            pstmt.setString(2, playerName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                punishments.add(extractPunishmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public List<Punishment> getActivePunishmentsByIP(String ip) {
        String sql = "SELECT * FROM punishments WHERE ip = ? AND active = 1";
        List<Punishment> punishments = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                punishments.add(extractPunishmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public List<Punishment> getPunishmentHistory(String playerName, UUID playerUUID) {
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? OR player_name = ? ORDER BY start_time DESC";
        List<Punishment> punishments = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID != null ? playerUUID.toString() : null);
            pstmt.setString(2, playerName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                punishments.add(extractPunishmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public List<Punishment> getAllActivePunishments() {
        String sql = "SELECT * FROM punishments WHERE active = 1";
        List<Punishment> punishments = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                punishments.add(extractPunishmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    private Punishment extractPunishmentFromResultSet(ResultSet rs) throws SQLException {
        Punishment punishment = new Punishment();
        punishment.setId(rs.getInt("id"));
        punishment.setPlayerName(rs.getString("player_name"));
        String uuidStr = rs.getString("player_uuid");
        if (uuidStr != null) {
            punishment.setPlayerUUID(UUID.fromString(uuidStr));
        }
        punishment.setIp(rs.getString("ip"));
        punishment.setStaffName(rs.getString("staff_name"));
        punishment.setStaffUUID(UUID.fromString(rs.getString("staff_uuid")));
        punishment.setReason(rs.getString("reason"));
        punishment.setType(Punishment.Type.valueOf(rs.getString("type")));
        punishment.setDuration(rs.getLong("duration"));
        punishment.setStartTime(rs.getLong("start_time"));
        punishment.setActive(rs.getBoolean("active"));
        punishment.setUnpardonTime(rs.getLong("unpardon_time"));
        punishment.setUnpardonStaff(rs.getString("unpardon_staff"));
        String unpardonStaffUUID = rs.getString("unpardon_staff_uuid");
        if (unpardonStaffUUID != null) {
            punishment.setUnpardonStaffUUID(UUID.fromString(unpardonStaffUUID));
        }
        return punishment;
    }
}