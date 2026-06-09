package com.b2la.dbroffice.security;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class AccessControl {
    private static final Map<BackOfficeRole, Set<Permission>> MATRIX = Map.of(
            BackOfficeRole.SUPER_ADMIN, EnumSet.allOf(Permission.class),
            BackOfficeRole.COMPLIANCE_AGENT, EnumSet.allOf(Permission.class),
            BackOfficeRole.ANALYST, EnumSet.of(
                    Permission.DASHBOARD_READ,
                    Permission.USERS_READ,
                    Permission.OPERATIONS_READ,
                    Permission.GPS_READ,
                    Permission.REPORTING_READ,
                    Permission.CONFIG_READ,
                    Permission.AUDIT_READ)
    );

    private AccessControl() {
    }

    public static boolean allows(BackOfficeRole role, Permission permission) {
        return role != null && MATRIX.getOrDefault(role, Set.of()).contains(permission);
    }

    public static void require(BackOfficeRole role, Permission permission) {
        if (!allows(role, permission)) {
            throw new SecurityException("Permission refusée: " + permission);
        }
    }
}
