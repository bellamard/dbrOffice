package com.b2la.dbroffice.security;

public enum BackOfficeRole {
    SUPER_ADMIN,
    BACK_OFFICE_AGENT,
    ANALYST;

    public static BackOfficeRole fromBackendRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Le compte ne contient aucun rôle.");
        }
        return switch (role.trim().toUpperCase()) {
            case "ADMIN", "SUPER_ADMIN", "SUPER ADMIN" -> SUPER_ADMIN;
            case "OFFICE", "BACK_OFFICE", "BACK_OFFICE_AGENT", "AGENT_BACK_OFFICE" -> BACK_OFFICE_AGENT;
            case "ANALYST", "ANALYSTE" -> ANALYST;
            default -> throw new IllegalArgumentException("Rôle non autorisé dans le back-office: " + role);
        };
    }
}
