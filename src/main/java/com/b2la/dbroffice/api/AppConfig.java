package com.b2la.dbroffice.api;

import java.util.prefs.Preferences;

public final class AppConfig {
    private static final String DEFAULT_BASE_URL = "https://dbr.b2la.online";
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String DEFAULT_ACCENT_COLOR = "#007AFF";
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

    public static String themeMode() {
        String configured = PREFS.get("ui.themeMode", THEME_SYSTEM).trim().toLowerCase();
        if (THEME_LIGHT.equals(configured) || THEME_DARK.equals(configured) || THEME_SYSTEM.equals(configured)) {
            return configured;
        }
        return THEME_SYSTEM;
    }

    public static void saveThemeMode(String themeMode) {
        if (themeMode == null || themeMode.isBlank()) {
            PREFS.put("ui.themeMode", THEME_SYSTEM);
            return;
        }
        String normalized = themeMode.trim().toLowerCase();
        if (!THEME_LIGHT.equals(normalized) && !THEME_DARK.equals(normalized) && !THEME_SYSTEM.equals(normalized)) {
            throw new IllegalArgumentException("Le mode de thème est invalide.");
        }
        PREFS.put("ui.themeMode", normalized);
    }

    public static String accentColor() {
        String configured = PREFS.get("ui.accentColor", DEFAULT_ACCENT_COLOR).trim();
        return configured.matches("^#[0-9a-fA-F]{6}$") ? configured.toUpperCase() : DEFAULT_ACCENT_COLOR;
    }

    public static void saveAccentColor(String color) {
        if (color == null || color.isBlank()) {
            PREFS.put("ui.accentColor", DEFAULT_ACCENT_COLOR);
            return;
        }
        String normalized = color.trim().toUpperCase();
        if (!normalized.matches("^#[0-9A-F]{6}$")) {
            throw new IllegalArgumentException("La couleur d'accent doit être au format #RRGGBB.");
        }
        PREFS.put("ui.accentColor", normalized);
    }

    public static boolean highContrastEnabled() {
        return PREFS.getBoolean("ui.highContrast", false);
    }

    public static void saveHighContrastEnabled(boolean enabled) {
        PREFS.putBoolean("ui.highContrast", enabled);
    }
}
