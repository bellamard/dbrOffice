package com.b2la.dbroffice.api;

public record ApiResponse(int statusCode, String body) {
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}
