package com.b2la.dbroffice.security;

public final class SessionContext {
    private static volatile BackOfficeRole role;
    private static volatile String username = "";

    private SessionContext() {
    }

    public static BackOfficeRole role() {
        return role;
    }

    public static void initialize(String currentUsername, BackOfficeRole currentRole) {
        username = currentUsername == null ? "" : currentUsername;
        role = currentRole;
    }

    public static String username() {
        return username;
    }

    public static void clear() {
        username = "";
        role = null;
    }
}
