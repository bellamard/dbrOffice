package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.api.ApiClient;
import com.b2la.dbroffice.api.ApiException;
import com.b2la.dbroffice.api.ApiResponse;
import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import com.b2la.dbroffice.security.BackOfficeRole;
import com.b2la.dbroffice.security.SessionContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private Label welcomeText, errorTitle;
    @FXML
    private Button btnConnexion, btnClose;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Pane paneLoading;



    @FXML
    protected void onConnexion(){
        String username = fieldUsername.getText() == null ? "" : fieldUsername.getText().trim();
        String password = fieldPassword.getText() == null ? "" : fieldPassword.getText();
        if (username.isBlank() || username.equals("+243")) {
            showLoginError("Le champ utilisateur est obligatoire.");
            return;
        }
        if (password.isBlank()) {
            showLoginError("Le mot de passe est obligatoire.");
            return;
        }

        paneLoading.setVisible(true);
        btnConnexion.setDisable(true);
        errorTitle.setVisible(false);
        Task<LoginSession> task = new Task<>() {
            @Override
            protected LoginSession call() {
                LoginResponse response = ApiClient.getInstance().login(new LoginRequest(username, password));
                ApiResponse profile = ApiClient.getInstance().send("GET", "/accounts/solde", "");
                String backendRole = extractRole(profile.body());
                return new LoginSession(response, BackOfficeRole.fromBackendRole(backendRole));
            }
        };
        task.setOnSucceeded(event -> {
            Storage.saveLogin(task.getValue().response());
            SessionContext.initialize(username, task.getValue().role());
            onEnterDashboard();
        });
        task.setOnFailed(event -> {
            Storage.removeLogin();
            paneLoading.setVisible(false);
            btnConnexion.setDisable(false);
            fieldPassword.clear();
            Throwable error = task.getException();
            showLoginError(error instanceof ApiException
                    ? "Connexion refusée par le backend."
                    : error.getMessage());
        });
        Thread thread = new Thread(task, "dbr-login");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void handleKeyPressed(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            onConnexion();
        }
    }

    @FXML
    protected void onClose() {
        Storage.removeLogin();
        System.exit(0);
    }

    private void onEnterDashboard(){
        try {
            FXMLLoader loader= new FXMLLoader(HelloApplication.class.getResource("admin-console.fxml"));
            Parent root = loader.load();
            Scene scene= new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
            Stage stage= (Stage)fieldUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("DASHBOARD");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showLoginError(String message) {
        errorTitle.setText(message);
        errorTitle.setVisible(true);
        paneLoading.setVisible(false);
        btnConnexion.setDisable(false);
    }

    private static String extractRole(String json) {
        JsonElement root = JsonParser.parseString(json);
        String role = findRole(root);
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le backend n'a retourné aucun rôle back-office.");
        }
        return role;
    }

    private static String findRole(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement role = object.get("role");
            if (role != null) {
                if (role.isJsonPrimitive()) {
                    return role.getAsString();
                }
                if (role.isJsonObject() && role.getAsJsonObject().has("libelle")) {
                    return role.getAsJsonObject().get("libelle").getAsString();
                }
            }
            for (String key : object.keySet()) {
                String nested = findRole(object.get(key));
                if (nested != null) {
                    return nested;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                String nested = findRole(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private record LoginSession(LoginResponse response, BackOfficeRole role) {
    }
}
