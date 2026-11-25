package com.b2la.dbroffice.api;

import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ApiClient {
    private static final ApiClient INSTANCE = new ApiClient();
    private static final Gson GSON = new Gson();
    private static final long MAX_KYC_FILE_SIZE = 5L * 1024 * 1024;
    private static final java.util.Set<String> KYC_EXTENSIONS = java.util.Set.of("pdf", "jpg", "jpeg", "png");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final Object refreshLock = new Object();

    private ApiClient() {
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public LoginResponse login(LoginRequest credentials) {
        ApiResponse response = sendWithoutAuthentication(
                "POST", "/auth/login", GSON.toJson(credentials), "application/json");
        if (!response.isSuccess()) {
            // Compatibility with the historical backend route.
            response = sendWithoutAuthentication(
                    "POST", "/users/connexion", GSON.toJson(credentials), "application/json");
        }
        ensureSuccess(response);
        LoginResponse login = loginResponse(response.body());
        if (login == null || login.getBearer() == null || login.getBearer().isBlank()) {
            throw new ApiException(response.statusCode(), "Réponse de connexion invalide.", response.body());
        }
        Storage.saveLogin(login);
        return login;
    }

    public ApiResponse send(String method, String path, String body) {
        return send(method, path, body, "application/json");
    }

    public ApiResponse send(String method, String path, String body, String contentType) {
        LoginResponse session = Storage.loadLogin();
        ApiResponse response = execute(method, path, body, contentType, session == null ? null : session.getBearer());
        if (response.statusCode() == 401 && session != null && refreshSession(session.getRefresh())) {
            LoginResponse refreshed = Storage.loadLogin();
            response = execute(method, path, body, contentType, refreshed == null ? null : refreshed.getBearer());
        }
        ensureSuccess(response);
        return response;
    }

    public ApiResponse get(String path, Map<String, String> query) {
        String suffix = query == null || query.isEmpty() ? "" : query.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&", "?", ""));
        return send("GET", path + suffix, "");
    }

    public ApiResponse sendMultipart(String method, String path, Map<String, String> fields, Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Le fichier KYC est introuvable.");
        }
        String filename = file.getFileName().toString();
        String extension = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT)
                : "";
        try {
            long size = Files.size(file);
            if (size <= 0 || size > MAX_KYC_FILE_SIZE) {
                throw new IllegalArgumentException("Le document doit peser entre 1 octet et 5 Mo.");
            }
        } catch (IOException exception) {
            throw new ApiException("Lecture du document KYC impossible.", exception);
        }
        if (!KYC_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Format refusé. Utilisez un fichier PDF, JPG ou PNG.");
        }
        String boundary = "----DbrOffice" + UUID.randomUUID();
        byte[] payload = multipartPayload(boundary, fields, file);
        LoginResponse session = Storage.loadLogin();
        ApiResponse response = executeBytes(
                method, path, payload, "multipart/form-data; boundary=" + boundary,
                session == null ? null : session.getBearer());
        if (response.statusCode() == 401 && session != null && refreshSession(session.getRefresh())) {
            LoginResponse refreshed = Storage.loadLogin();
            response = executeBytes(
                    method, path, payload, "multipart/form-data; boundary=" + boundary,
                    refreshed == null ? null : refreshed.getBearer());
        }
        ensureSuccess(response);
        return response;
    }

    public void logout() {
        try {
            send("POST", "/users/deconnexion", "");
        } finally {
            Storage.removeLogin();
        }
    }

    private ApiResponse sendWithoutAuthentication(String method, String path, String body, String contentType) {
        return execute(method, path, body, contentType, null);
    }

    private ApiResponse execute(String method, String path, String body, String contentType, String token) {
        HttpRequest.BodyPublisher publisher = body == null || body.isBlank()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        return executePublisher(method, path, publisher, body != null && !body.isBlank(), contentType, token);
    }

    private ApiResponse executeBytes(
            String method, String path, byte[] body, String contentType, String token) {
        return executePublisher(
                method, path, HttpRequest.BodyPublishers.ofByteArray(body), true, contentType, token);
    }

    private ApiResponse executePublisher(
            String method, String path, HttpRequest.BodyPublisher publisher, boolean hasBody,
            String contentType, String token) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.baseUrl() + normalizePath(path)))
                    .timeout(Duration.ofSeconds(45))
                    .header("Accept", "application/json");
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            String deviceCertificate = AppConfig.deviceCertificate();
            if (!deviceCertificate.isBlank()) {
                builder.header("X-Device-Certificate", deviceCertificate);
            }
            if (hasBody) {
                builder.header("Content-Type", contentType);
            }
            builder.method(method.toUpperCase(), publisher);
            HttpResponse<String> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new ApiResponse(response.statusCode(), response.body());
        } catch (IOException exception) {
            throw new ApiException("Impossible de joindre l'API " + AppConfig.baseUrl(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException("Appel API interrompu.", exception);
        }
    }

    private static byte[] multipartPayload(String boundary, Map<String, String> fields, Path file) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (Map.Entry<String, String> field : fields.entrySet()) {
                output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                output.write(("Content-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8));
                output.write(field.getValue().getBytes(StandardCharsets.UTF_8));
                output.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            String safeFilename = file.getFileName().toString().replaceAll("[\\r\\n\\\"]", "_");
            output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" +
                    safeFilename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            output.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            output.write(Files.readAllBytes(file));
            output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return output.toByteArray();
        } catch (IOException exception) {
            throw new ApiException("Lecture du document KYC impossible.", exception);
        }
    }

    private boolean refreshSession(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        synchronized (refreshLock) {
            LoginResponse current = Storage.loadLogin();
            if (current != null && !refreshToken.equals(current.getRefresh())) {
                return true;
            }
            ApiResponse response = sendWithoutAuthentication(
                    "POST",
                    "/users/refresh-token",
                    GSON.toJson(Map.of("refresh", refreshToken)),
                    "application/json");
            if (!response.isSuccess()) {
                Storage.removeLogin();
                return false;
            }
            LoginResponse refreshed = loginResponse(response.body());
            if (refreshed == null || refreshed.getBearer() == null) {
                Storage.removeLogin();
                return false;
            }
            if (refreshed.getRefresh() == null || refreshed.getRefresh().isBlank()) {
                refreshed.setRefresh(refreshToken);
            }
            Storage.saveLogin(refreshed);
            return true;
        }
    }

    private static void ensureSuccess(ApiResponse response) {
        if (!response.isSuccess()) {
            throw new ApiException(
                    response.statusCode(),
                    "Erreur API HTTP " + response.statusCode(),
                    response.body());
        }
    }

    private static LoginResponse loginResponse(String body) {
        JsonElement root = JsonParser.parseString(body);
        if (root != null && root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            JsonElement data = object.get("data");
            if (data != null && data.isJsonObject()) {
                root = data;
            }
        }
        return GSON.fromJson(root, LoginResponse.class);
    }

    private static String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
