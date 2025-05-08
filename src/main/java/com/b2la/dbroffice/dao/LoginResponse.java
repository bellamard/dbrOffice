package com.b2la.dbroffice.dao;

public class LoginResponse {

    private String bearer, refresh;

    public LoginResponse(String bearer, String refresh) {
        this.bearer = bearer;
        this.refresh = refresh;
    }

    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}
