package com.b2la.dbroffice.api;

import java.util.prefs.Preferences;

public final class AppConfig {
    private static final String DEFAULT_BASE_URL = "https://dbr.b2la.online";
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);

    private AppConfig() {
    }

    public static String baseUrl() {
        String environmentValue = System.getenv("DBR_API_BASE_URL");
        String configured = environmentValue == null || environmentValue.isBlank()
                ? PREFS.get("api.baseUrl", DEFAULT_BASE_URL)
                : environmentValue;
        return configured.replaceAll("/+$", "");
    }

    public static String endpoint(String path) {
        if (path == null || path.isBlank()) {
            return baseUrl();
        }
        return baseUrl() + (path.startsWith("/") ? path : "/" + path);
    }

    public static void saveBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("L'URL de l'API est obligatoire.");
        }
        PREFS.put("api.baseUrl", baseUrl.trim().replaceAll("/+$", ""));
    }

    public static String deviceCertificate() {
        return PREFS.get("api.deviceCertificate", "").trim();
    }

    public static void saveDeviceCertificate(String certificate) {
        if (certificate == null || certificate.isBlank()) {
            PREFS.remove("api.deviceCertificate");
            return;
        }
        PREFS.put("api.deviceCertificate", certificate.trim());
    }
}
