package com.b2la.dbroffice.security;

public enum BackOfficeRole {
    ADMIN,
    OFFICE;

    public static BackOfficeRole fromBackendRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Le compte ne contient aucun rôle.");
        }
        return switch (role.trim().toUpperCase()) {
            case "ADMIN", "SUPER_ADMIN", "SUPER ADMIN" -> ADMIN;
            case "OFFICE", "COMPLIANCE", "COMPLIANCE_AGENT", "AGENT_COMPLIANCE",
                    "BACK_OFFICE", "BACK_OFFICE_AGENT", "AGENT_BACK_OFFICE" -> OFFICE;
            default -> throw new IllegalArgumentException("Rôle non autorisé dans le back-office: " + role);
        };
    }
}
