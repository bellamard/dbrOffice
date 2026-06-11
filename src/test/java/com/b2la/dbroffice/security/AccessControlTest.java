package com.b2la.dbroffice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlTest {
    @Test
    void adminHasEveryPermission() {
        for (Permission permission : Permission.values()) {
            assertTrue(AccessControl.allows(BackOfficeRole.ADMIN, permission));
        }
    }

    @Test
    void officeRoleHasOperationalButNotAdministrativePermissions() {
        assertTrue(AccessControl.allows(BackOfficeRole.OFFICE, Permission.KYC_VALIDATE));
        assertTrue(AccessControl.allows(BackOfficeRole.OFFICE, Permission.OPERATIONS_READ));
        assertTrue(AccessControl.allows(BackOfficeRole.OFFICE, Permission.MESSAGING_WRITE));
        assertTrue(AccessControl.allows(BackOfficeRole.OFFICE, Permission.GPS_READ));
        assertFalse(AccessControl.allows(BackOfficeRole.OFFICE, Permission.OPERATIONS_WRITE));
        assertFalse(AccessControl.allows(BackOfficeRole.OFFICE, Permission.CONFIG_WRITE));
        assertFalse(AccessControl.allows(BackOfficeRole.OFFICE, Permission.USERS_WRITE));
        assertFalse(AccessControl.allows(BackOfficeRole.OFFICE, Permission.AUDIT_READ));
    }

    @Test
    void backendRolesAreMappedExplicitly() {
        assertEquals(BackOfficeRole.ADMIN, BackOfficeRole.fromBackendRole("ADMIN"));
        assertEquals(BackOfficeRole.OFFICE, BackOfficeRole.fromBackendRole("OFFICE"));
        assertThrows(IllegalArgumentException.class, () -> BackOfficeRole.fromBackendRole("CLIENT"));
        assertThrows(IllegalArgumentException.class, () -> BackOfficeRole.fromBackendRole("ANALYSTE"));
    }
}
