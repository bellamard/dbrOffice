package com.b2la.dbroffice.dao;

public class Accounts {
        int id;
        float cdf, usd;
        User user;

    public Accounts(int id, float cdf, float usd, User user) {
        this.id = id;
        this.cdf = cdf;
        this.usd = usd;
        this.user = user;
    }

    public Accounts() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
