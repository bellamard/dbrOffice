package com.b2la.dbroffice.dao;

public class CountUsers {
    private float cdf, usd;
    private int id;
    private User user;

    public CountUsers(float cdf, float usd, int id, User user) {
        this.cdf = cdf;
        this.usd = usd;
        this.id = id;
        this.user = user;
    }

    public float getCdf() {
        return cdf;
    }

    public void setCdf(float cdf) {
        this.cdf = cdf;
    }

    public float getUsd() {
        return usd;
    }

    public void setUsd(float usd) {
        this.usd = usd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CountUsers() {
    }
}
