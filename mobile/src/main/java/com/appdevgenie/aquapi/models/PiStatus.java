package com.appdevgenie.aquapi.models;

public class PiStatus {

    private String ip;
    private long lastOnlinePi;
    private boolean onlinePi;
    private long lastOnlineArduino;
    private boolean onlineArduino;


    public PiStatus() {
    }

    public PiStatus(String ip, long lastOnlinePi, boolean onlinePi, long lastOnlineArduino, boolean onlineArduino) {
        this.ip = ip;
        this.lastOnlinePi = lastOnlinePi;
        this.onlinePi = onlinePi;
        this.lastOnlineArduino = lastOnlineArduino;
        this.onlineArduino = onlineArduino;
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
}
