package com.b2la.dbroffice.api;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class EndpointCatalogTest {
    @Test
    void catalogCoversEveryBackOfficeModule() {
        var modules = EndpointCatalog.all().stream()
                .map(EndpointDefinition::module)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(modules.contains("Utilisateurs"));
        assertTrue(modules.contains("Opérations"));
        assertTrue(modules.contains("KYC"));
        assertTrue(modules.contains("GPS"));
        assertTrue(modules.contains("Reporting"));
        assertTrue(modules.contains("Configuration"));
        assertTrue(EndpointCatalog.all().size() >= 90);
    }

    @Test
    void endpointNamesAreUniqueInsideTheirModule() {
        var keys = new HashSet<String>();
        EndpointCatalog.all().forEach(endpoint ->
                assertTrue(keys.add(endpoint.module() + ":" + endpoint.name()),
                        "Endpoint dupliqué: " + endpoint));
    }

    @Test
    void everyEndpointHasADeclaredPermission() {
        EndpointCatalog.all().forEach(endpoint -> {
            assertNotNull(endpoint.permission());
            assertFalse(endpoint.path().isBlank());
            assertFalse(endpoint.method().isBlank());
        });
    }
}
