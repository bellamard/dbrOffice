package com.b2la.dbroffice.api;

public record AuditEntry(String timestamp, String username, String action, String target, String result) {
}
