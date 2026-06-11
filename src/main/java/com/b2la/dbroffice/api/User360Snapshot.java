package com.b2la.dbroffice.api;

import java.util.Map;

public record User360Snapshot(String userId, Map<String, String> sections) {
}
