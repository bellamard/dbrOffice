package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.api.ApiClient;
import com.b2la.dbroffice.api.ApiException;
import com.b2la.dbroffice.api.ApiResponse;
import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import com.b2la.dbroffice.security.BackendRoleResolver;
import com.b2la.dbroffice.security.BackOfficeRole;
import com.b2la.dbroffice.security.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    private static final double COMPACT_WIDTH = 780;
    private static final double SHORT_HEIGHT = 600;

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
    private BorderPane loginRoot;
    @FXML
    private VBox loginHero;
    @FXML
    private StackPane loginFormZone;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            updateResponsiveLayout(loginRoot.getWidth(), loginRoot.getHeight());
            loginRoot.widthProperty().addListener((observable, oldValue, width) ->
                    updateResponsiveLayout(width.doubleValue(), loginRoot.getHeight()));
            loginRoot.heightProperty().addListener((observable, oldValue, height) ->
                    updateResponsiveLayout(loginRoot.getWidth(), height.doubleValue()));
        });
    }

    private void updateResponsiveLayout(double width, double height) {
        boolean compact = width < COMPACT_WIDTH;
        loginHero.setVisible(!compact);
        loginHero.setManaged(!compact);
        if (!compact) {
            loginHero.setPrefWidth(Math.max(360, Math.min(520, width * 0.52)));
        }
        setStyleClass(loginRoot, "login-compact", compact);
        setStyleClass(loginRoot, "login-short", height < SHORT_HEIGHT);
    }

    private static void setStyleClass(Pane pane, String styleClass, boolean enabled) {
        if (enabled && !pane.getStyleClass().contains(styleClass)) {
            pane.getStyleClass().add(styleClass);
        } else if (!enabled) {
            pane.getStyleClass().remove(styleClass);
        }
    }

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
                ApiResponse profile = ApiClient.getInstance().send("GET", "/users/profil", "");
                return new LoginSession(response, BackendRoleResolver.fromProfile(profile.body()));
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
            showLoginError(loginErrorMessage(error));
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
        if (loginRoot.getScene() != null && loginRoot.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
        Platform.exit();
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

    private static String loginErrorMessage(Throwable error) {
        if (error instanceof ApiException apiError) {
            String detail = apiError.getResponseBody();
            if (detail != null && !detail.isBlank()) {
                return "Connexion refusée (" + apiError.getStatusCode() + "): " + detail;
            }
            return apiError.getMessage();
        }
        return error.getMessage() == null ? "Connexion impossible." : error.getMessage();
    }

    private record LoginSession(LoginResponse response, BackOfficeRole role) {
    }
}
