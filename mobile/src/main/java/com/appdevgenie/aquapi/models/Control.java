package com.appdevgenie.aquapi.models;

public class Control {

    private boolean bypass;
    private boolean light;
    private boolean rebootPi;
    private boolean resetArduino;

    public Control() {
    }

    public Control(boolean bypass, boolean light, boolean rebootPi, boolean resetArduino) {
        this.bypass = bypass;
        this.light = light;
        this.rebootPi = rebootPi;
        this.resetArduino = resetArduino;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isLight() {
        return light;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public boolean isRebootPi() {
        return rebootPi;
    }

    public void setRebootPi(boolean rebootPi) {
        this.rebootPi = rebootPi;
    }

    public boolean isResetArduino() {
        return resetArduino;
    }

    public void setResetArduino(boolean resetArduino) {
        this.resetArduino = resetArduino;
    }
}
