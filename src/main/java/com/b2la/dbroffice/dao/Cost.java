package com.b2la.dbroffice.dao;

public class Cost {
    private String devices;
    private float max,min, percent;

    public Cost(String devices, float max, float min, float percent) {
        this.devices = devices;
        this.max = max;
        this.min = min;
        this.percent = percent;
    }

    public Cost() {
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}
