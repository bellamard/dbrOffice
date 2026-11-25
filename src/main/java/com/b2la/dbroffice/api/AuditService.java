package com.b2la.dbroffice.api;

import com.b2la.dbroffice.security.SessionContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AuditService {
    private static final AuditService INSTANCE = new AuditService();
    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());
    private final Path auditFile = Path.of(System.getProperty("user.home"), ".dbr-office", "audit.csv");
    private volatile String persistenceError;

    private AuditService() {
        load();
    }

    public static AuditService getInstance() {
        return INSTANCE;
    }

    public void record(String action, String target, String result) {
        AuditEntry entry = new AuditEntry(
                OffsetDateTime.now().toString(),
                SessionContext.username(),
                sanitize(action),
                sanitize(target),
                sanitize(result));
        entries.add(entry);
        try {
            Files.createDirectories(auditFile.getParent());
            Files.writeString(
                    auditFile,
                    toCsv(entry) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            persistenceError = null;
        } catch (IOException | SecurityException exception) {
            persistenceError = "Le journal local n'a pas pu être enregistré sur ce poste.";
        }
    }

    public String persistenceError() {
        return persistenceError;
    }

    public List<AuditEntry> entries() {
        synchronized (entries) {
            return List.copyOf(entries);
        }
    }

    private void load() {
        if (!Files.exists(auditFile)) {
            return;
        }
        try {
            Files.readAllLines(auditFile, StandardCharsets.UTF_8).stream()
                    .map(line -> line.split(",", 5))
                    .filter(parts -> parts.length == 5)
                    .map(parts -> new AuditEntry(parts[0], parts[1], parts[2], parts[3], parts[4]))
                    .forEach(entries::add);
        } catch (IOException | SecurityException exception) {
            entries.clear();
            persistenceError = "Le journal local existant n'a pas pu être lu sur ce poste.";
        }
    }

    private static String toCsv(AuditEntry entry) {
        return String.join(",",
                entry.timestamp(), entry.username(), entry.action(), entry.target(), entry.result());
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace(',', ';').replace('\n', ' ').replace('\r', ' ');
    }
}
