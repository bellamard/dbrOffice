package com.b2la.dbroffice.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {
    @Test
    void endpointJoinsBaseUrlAndPath() {
        assertEquals(AppConfig.baseUrl() + "/users", AppConfig.endpoint("/users"));
        assertEquals(AppConfig.baseUrl() + "/users", AppConfig.endpoint("users"));
        assertEquals(AppConfig.baseUrl(), AppConfig.endpoint(""));
    }
}
