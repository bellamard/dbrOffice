package com.b2la.dbroffice.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public final class BackendRoleResolver {
    private static final List<String> ROLE_KEYS = List.of(
            "customerType", "customerCategory", "type", "role");

    private BackendRoleResolver() {
    }

    public static BackOfficeRole fromProfile(String json) {
        String role = findRole(JsonParser.parseString(json));
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le profil backend ne contient aucun rôle.");
        }
        return BackOfficeRole.fromBackendRole(role);
    }

    private static String findRole(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : ROLE_KEYS) {
                String role = readRole(object.get(key));
                if (role != null && !role.isBlank()) {
                    return role;
                }
            }
            for (String key : object.keySet()) {
                String role = findRole(object.get(key));
                if (role != null) {
                    return role;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                String role = findRole(child);
                if (role != null) {
                    return role;
                }
            }
        }
        return null;
    }

    private static String readRole(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (element.isJsonObject()) {
            JsonElement label = element.getAsJsonObject().get("libelle");
            return label != null && label.isJsonPrimitive() ? label.getAsString() : null;
        }
        return null;
    }
}
