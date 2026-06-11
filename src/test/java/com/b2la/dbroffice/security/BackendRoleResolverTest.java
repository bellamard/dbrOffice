package com.b2la.dbroffice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendRoleResolverTest {
    @Test
    void readsRoleFromCurrentIso20022Profile() {
        String profile = """
                {
                  "status": "success",
                  "data": {
                    "response": {
                      "customer": {
                        "customerType": "ADMIN",
                        "bccCompliance": {"customerCategory": "ADMIN"}
                      }
                    }
                  }
                }
                """;

        assertEquals(BackOfficeRole.ADMIN, BackendRoleResolver.fromProfile(profile));
    }

    @Test
    void readsOfficeFromHistoricalProfile() {
        assertEquals(
                BackOfficeRole.OFFICE,
                BackendRoleResolver.fromProfile("{\"data\":{\"role\":{\"libelle\":\"OFFICE\"}}}"));
    }
}
