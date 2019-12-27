package com.appdevgenie.aquapi.models;

public class Control {

    private boolean bypassOn;
    private boolean lightOn;
    private boolean rebootPi;
    private boolean resetArduino;

    public Control() {
    }

    public Control(boolean bypassOn, boolean lightOn, boolean rebootPi, boolean resetArduino) {
        this.bypassOn = bypassOn;
        this.lightOn = lightOn;
        this.rebootPi = rebootPi;
        this.resetArduino = resetArduino;
    }

    public boolean isBypassOn() {
        return bypassOn;
    }

    public void setBypassOn(boolean bypassOn) {
        this.bypassOn = bypassOn;
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
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
