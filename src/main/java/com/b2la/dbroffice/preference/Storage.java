package com.b2la.dbroffice.preference;

import com.b2la.dbroffice.dao.LoginResponse;
import com.google.gson.Gson;

import java.util.prefs.Preferences;

public class Storage {
    private static final Preferences prefs =Preferences.userNodeForPackage(Storage.class);
    private static Gson gson=new Gson();

    public static void saveLogin(LoginResponse response){
        String json= gson.toJson(response);
        prefs.put("token", json);
    }

    public static LoginResponse loadLogin(){
        String json= prefs.get("token", null);
        if(json!=null){
            return gson.fromJson(json, LoginResponse.class);
        }
        return null;
    }

    public static void removeLogin(){
        prefs.remove("token");
    }
}
