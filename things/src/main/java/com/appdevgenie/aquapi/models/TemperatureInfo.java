package com.appdevgenie.aquapi.models;

import java.util.ArrayList;
import java.util.Map;

public class TemperatureInfo {

    private String name;
    private float temp;
    private float tempMin;
    private float tempMax;
    private long minTimeStamp;
    private long maxTimeStamp;

    public TemperatureInfo() {
    }

    public TemperatureInfo(String name, float temp, float tempMin, float tempMax, long minTimeStamp, long maxTimeStamp) {
        this.name = name;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.minTimeStamp = minTimeStamp;
        this.maxTimeStamp = maxTimeStamp;
    }

    public static ArrayList<TemperatureInfo> createTempList(){

        ArrayList<TemperatureInfo> arrayList = new ArrayList<>();
        arrayList.add(new TemperatureInfo("water", 0f, 50f, 0, 0, 0));
        arrayList.add(new TemperatureInfo("system", 0f, 50f, 0, 0, 0));
        arrayList.add(new TemperatureInfo("room", 0f, 50f, 0, 0, 0));

        return arrayList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getTempMin() {
        return tempMin;
    }

    public void setTempMin(float tempMin) {
        this.tempMin = tempMin;
    }

    public float getTempMax() {
        return tempMax;
    }

    public void setTempMax(float tempMax) {
        this.tempMax = tempMax;
    }

    public long getMinTimeStamp() {
        return minTimeStamp;
    }

    public void setMinTimeStamp(long minTimeStamp) {
        this.minTimeStamp = minTimeStamp;
    }

    public long getMaxTimeStamp() {
        return maxTimeStamp;
    }

    public void setMaxTimeStamp(long maxTimeStamp) {
        this.maxTimeStamp = maxTimeStamp;
    }

}
