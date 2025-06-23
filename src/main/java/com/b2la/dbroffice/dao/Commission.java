package com.b2la.dbroffice.dao;

public class Commission {
    int id;
    float usdwin, cdfwin;
    User user;

    public Commission(int id, float usdwin, float cdfwin, User user) {
        this.id = id;
        this.usdwin = usdwin;
        this.cdfwin = cdfwin;
        this.user = user;
    }

    public Commission() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getUsdwin() {
        return usdwin;
    }

    public void setUsdwin(float usdwin) {
        this.usdwin = usdwin;
    }

    public float getCdfwin() {
        return cdfwin;
    }

    public void setCdfwin(float cdfwin) {
        this.cdfwin = cdfwin;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
