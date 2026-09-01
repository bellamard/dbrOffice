package com.b2la.dbroffice.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class User360Service {
    private static final User360Service INSTANCE = new User360Service();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> USER_ID_KEYS = Set.of(
            "userid", "user_id", "iduser", "id_user", "idexp", "idben",
            "idclient", "identite", "idcreator", "id_creator", "idagent",
            "id_agent", "idusers", "id_users");
    private static final Set<String> USER_OBJECT_KEYS = Set.of(
            "user", "users", "exp", "ben", "client", "entite", "creator", "agent",
            "owner", "holder", "customer", "accountowner", "accountholder");
    private static final Set<String> COLLECTION_KEYS = Set.of(
            "acctlist", "accounts", "operations", "conversions", "kycs",
            "jetonscash", "etablissements", "messages", "gps", "items", "content");

    private final ApiClient api = ApiClient.getInstance();

    private User360Service() {
    }

    public static User360Service getInstance() {
        return INSTANCE;
    }

    public User360Snapshot load(String userId, String username) {
        LinkedHashMap<String, String> sections = new LinkedHashMap<>();
        sections.put("Résumé", api.send("GET", "/users/" + userId, "").body());
        addFiltered(sections, "Comptes", "/accounts", userId, username);
        addFiltered(sections, "Opérations", "/operations", userId, username);
        addFiltered(sections, "Conversions", "/conversions", userId, username);
        addFiltered(sections, "KYC", "/kyc/list", userId, username);
        addFiltered(sections, "Jetons cash", "/jetons-cash", userId, username);
        addFiltered(sections, "Établissements", "/etablissements", userId, username);
        addFiltered(sections, "Messages", "/messagerie", userId, username);
        addFiltered(sections, "GPS", "/gps/all", userId, username);
        addAudit(sections, username);
        return new User360Snapshot(
                userId, Collections.unmodifiableMap(new LinkedHashMap<>(sections)));
    }

    private void addFiltered(
            Map<String, String> sections, String title, String path, String userId, String username) {
        try {
            sections.put(title, filterResponse(api.send("GET", path, "").body(), userId, username));
        } catch (ApiException exception) {
            sections.put(title, unavailable(exception));
        }
    }

    private void addAudit(Map<String, String> sections, String username) {
        if (username == null || username.isBlank()) {
            sections.put("Audit", "[]");
            return;
        }
        try {
            sections.put("Audit", api.get(
                    "/reporting/actions/user", Map.of("username", username)).body());
        } catch (ApiException exception) {
            sections.put("Audit", unavailable(exception));
        }
    }

    static String filterResponse(String body, String userId, String username) {
        JsonElement root = JsonParser.parseString(body);
        JsonElement payload = unwrap(root);
        if (!payload.isJsonArray()) {
            return GSON.toJson(payload);
        }
        JsonArray filtered = new JsonArray();
        for (JsonElement item : payload.getAsJsonArray()) {
            if (matches(item, userId, username)) {
                filtered.add(item);
            }
        }
        return GSON.toJson(filtered);
    }

    private static boolean matches(JsonElement element, String userId, String username) {
        if (element == null || element.isJsonNull()) {
            return false;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (matches(child, userId, username)) return true;
            }
            return false;
        }
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = normalize(entry.getKey());
            JsonElement value = entry.getValue();
            if (USER_ID_KEYS.contains(key) && primitiveEquals(value, userId)) {
                return true;
            }
            if (USER_OBJECT_KEYS.contains(key) && nestedIdEquals(value, userId)) {
                return true;
            }
            if (Set.of("username", "phone", "email").contains(key)
                    && username != null && !username.isBlank()
                    && primitiveEquals(value, username)) {
                return true;
            }
            if ((value.isJsonObject() || value.isJsonArray()) && matches(value, userId, username)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nestedIdEquals(JsonElement value, String userId) {
        if (value == null || !value.isJsonObject()) {
            return false;
        }
        JsonElement id = value.getAsJsonObject().get("id");
        return primitiveEquals(id, userId);
    }

    private static boolean primitiveEquals(JsonElement value, String expected) {
        return value != null && value.isJsonPrimitive()
                && value.getAsString().trim().equalsIgnoreCase(expected.trim());
    }

    private static JsonElement unwrap(JsonElement root) {
        JsonElement current = root;
        boolean unwrapped;
        do {
            unwrapped = false;
            if (!current.isJsonObject()) {
                break;
            }
            JsonObject object = current.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = normalize(entry.getKey());
                if ("data".equals(key) || "response".equals(key) || COLLECTION_KEYS.contains(key)) {
                    current = entry.getValue();
                    unwrapped = true;
                    break;
                }
            }
        } while (unwrapped);
        return current;
    }

    private static String normalize(String key) {
        return key.replace("-", "").toLowerCase(Locale.ROOT);
    }

    private static String unavailable(ApiException exception) {
        return "{\"indisponible\":true,\"status\":" + exception.getStatusCode()
                + ",\"message\":\"Endpoint non disponible pour ce rôle\"}";
    }
}
