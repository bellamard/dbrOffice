package com.b2la.dbroffice.preference;

import com.b2la.dbroffice.dao.LoginResponse;

import java.util.prefs.Preferences;

public class Storage {
    private static final Preferences legacyPreferences = Preferences.userNodeForPackage(Storage.class);
    private static volatile LoginResponse activeLogin;

    static {
        // Remove tokens persisted by older builds. Sessions now live only for this process.
        try {
            legacyPreferences.remove("token");
        } catch (SecurityException ignored) {
            // The persisted value is never read by this version.
        }
    }

    public static void saveLogin(LoginResponse response){
        activeLogin = response;
    }

    public static LoginResponse loadLogin(){
        return activeLogin;
    }

    public static void removeLogin(){
        activeLogin = null;
    }
}
