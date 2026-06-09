package com.b2la.dbroffice.api;

import com.b2la.dbroffice.security.Permission;

public record EndpointDefinition(
        String module,
        String name,
        String method,
        String path,
        String sampleBody,
        String contentType,
        Permission permission
) {
    @Override
    public String toString() {
        return method + "  " + name;
    }
}
