package com.appdevgenie.aquapi.models;

public class Temperatures {

    private float tempRoom;
    private float tempSystem;
    private float tempWater;

    public Temperatures() {
    }

    public Temperatures(float tempRoom, float tempSystem, float tempWater) {
        this.tempRoom = tempRoom;
        this.tempSystem = tempSystem;
        this.tempWater = tempWater;
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
