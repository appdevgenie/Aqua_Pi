package com.appdevgenie.aquapi.models;

public class PiStatus {

    private String ip;
    private long lastOnlinePi;
    private boolean onlinePi;
    private long lastOnlineArduino;
    private boolean onlineArduino;
    private float tempRoom;
    private float tempSystem;
    private float tempWater;

    public PiStatus() {
    }

    public PiStatus(String ip, long lastOnlinePi, boolean onlinePi, long lastOnlineArduino, boolean onlineArduino, float tempRoom, float tempSystem, float tempWater) {
        this.ip = ip;
        this.lastOnlinePi = lastOnlinePi;
        this.onlinePi = onlinePi;
        this.lastOnlineArduino = lastOnlineArduino;
        this.onlineArduino = onlineArduino;
        this.tempRoom = tempRoom;
        this.tempSystem = tempSystem;
        this.tempWater = tempWater;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getLastOnlinePi() {
        return lastOnlinePi;
    }

    public void setLastOnlinePi(long lastOnlinePi) {
        this.lastOnlinePi = lastOnlinePi;
    }

    public boolean isOnlinePi() {
        return onlinePi;
    }

    public void setOnlinePi(boolean onlinePi) {
        this.onlinePi = onlinePi;
    }

    public long getLastOnlineArduino() {
        return lastOnlineArduino;
    }

    public void setLastOnlineArduino(long lastOnlineArduino) {
        this.lastOnlineArduino = lastOnlineArduino;
    }

    public boolean isOnlineArduino() {
        return onlineArduino;
    }

    public void setOnlineArduino(boolean onlineArduino) {
        this.onlineArduino = onlineArduino;
    }

    public float getTempRoom() {
        return tempRoom;
    }

    public void setTempRoom(float tempRoom) {
        this.tempRoom = tempRoom;
    }

    public float getTempSystem() {
        return tempSystem;
    }

    public void setTempSystem(float tempSystem) {
        this.tempSystem = tempSystem;
    }

    public float getTempWater() {
        return tempWater;
    }

    public void setTempWater(float tempWater) {
        this.tempWater = tempWater;
    }
}
