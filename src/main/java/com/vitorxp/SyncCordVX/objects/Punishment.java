package com.vitorxp.SyncCordVX.objects;

import java.util.UUID;

public class Punishment {

    public enum Type {
        BAN, BAN_IP, MUTE, KICK
    }

    private int id;
    private String playerName;
    private UUID playerUUID;
    private String ip;
    private String staffName;
    private UUID staffUUID;
    private String reason;
    private Type type;
    private long duration;
    private long startTime;
    private boolean active;
    private long unpardonTime;
    private String unpardonStaff;
    private UUID unpardonStaffUUID;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public UUID getStaffUUID() { return staffUUID; }
    public void setStaffUUID(UUID staffUUID) { this.staffUUID = staffUUID; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getUnpardonTime() { return unpardonTime; }
    public void setUnpardonTime(long unpardonTime) { this.unpardonTime = unpardonTime; }

    public String getUnpardonStaff() { return unpardonStaff; }
    public void setUnpardonStaff(String unpardonStaff) { this.unpardonStaff = unpardonStaff; }

    public UUID getUnpardonStaffUUID() { return unpardonStaffUUID; }
    public void setUnpardonStaffUUID(UUID unpardonStaffUUID) { this.unpardonStaffUUID = unpardonStaffUUID; }

    public boolean isPermanent() {
        return duration == -1;
    }

    public long getEndTime() {
        if (isPermanent()) {
            return -1;
        }
        return startTime + duration;
    }

    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() > getEndTime();
    }
}