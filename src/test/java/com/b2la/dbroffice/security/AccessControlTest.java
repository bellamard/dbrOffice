package com.b2la.dbroffice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlTest {
    @Test
    void superAdminHasEveryPermission() {
        for (Permission permission : Permission.values()) {
            assertTrue(AccessControl.allows(BackOfficeRole.SUPER_ADMIN, permission));
        }
    }

    @Test
    void analystCannotMutateFinancialOrConfigurationData() {
        assertTrue(AccessControl.allows(BackOfficeRole.ANALYST, Permission.REPORTING_READ));
        assertTrue(AccessControl.allows(BackOfficeRole.ANALYST, Permission.GPS_READ));
        assertFalse(AccessControl.allows(BackOfficeRole.ANALYST, Permission.OPERATIONS_WRITE));
        assertFalse(AccessControl.allows(BackOfficeRole.ANALYST, Permission.CONFIG_WRITE));
    }

    @Test
    void officeRoleMatchesBackendBackOfficePermissions() {
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.KYC_VALIDATE));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.OPERATIONS_READ));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.MESSAGING_WRITE));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.OPERATIONS_WRITE));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.CONFIG_WRITE));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.USERS_WRITE));
        assertTrue(AccessControl.allows(BackOfficeRole.COMPLIANCE_AGENT, Permission.GPS_READ));
    }

    @Test
    void backendRolesAreMappedExplicitly() {
        assertEquals(BackOfficeRole.SUPER_ADMIN, BackOfficeRole.fromBackendRole("ADMIN"));
        assertEquals(BackOfficeRole.COMPLIANCE_AGENT, BackOfficeRole.fromBackendRole("OFFICE"));
        assertEquals(BackOfficeRole.ANALYST, BackOfficeRole.fromBackendRole("ANALYSTE"));
        assertThrows(IllegalArgumentException.class, () -> BackOfficeRole.fromBackendRole("CLIENT"));
    }
}
