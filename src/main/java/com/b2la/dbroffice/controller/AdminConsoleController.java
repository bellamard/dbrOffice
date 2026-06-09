package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.api.*;
import com.b2la.dbroffice.security.AccessControl;
import com.b2la.dbroffice.security.Permission;
import com.b2la.dbroffice.security.SessionContext;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminConsoleController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    @FXML private Label identityLabel;
    @FXML private Label usersKpi;
    @FXML private Label operationsKpi;
    @FXML private Label kycKpi;
    @FXML private Label apiStatusKpi;
    @FXML private ComboBox<String> moduleCombo;
    @FXML private ComboBox<EndpointDefinition> endpointCombo;
    @FXML private TextField pathField;
    @FXML private TextArea variablesArea;
    @FXML private TextArea bodyArea;
    @FXML private TextArea responseArea;
    @FXML private Button executeButton;
    @FXML private ProgressIndicator progress;
    @FXML private TableView<Map<String, String>> resultTable;
    @FXML private TableView<AuditEntry> auditTable;
    @FXML private TableColumn<AuditEntry, String> auditDate;
    @FXML private TableColumn<AuditEntry, String> auditUser;
    @FXML private TableColumn<AuditEntry, String> auditAction;
    @FXML private TableColumn<AuditEntry, String> auditTarget;
    @FXML private TableColumn<AuditEntry, String> auditResult;
    @FXML private BarChart<String, Number> operationChart;
    @FXML private TextField baseUrlField;
    @FXML private WebView gpsMap;

    private final ApiClient api = ApiClient.getInstance();
    private final AuditService audit = AuditService.getInstance();
    private final List<EndpointDefinition> authorizedEndpoints = new ArrayList<>();
    private String lastResponse = "";

    @FXML
    public void initialize() {
        identityLabel.setText(SessionContext.username() + "  |  " + SessionContext.role());
        baseUrlField.setText(AppConfig.baseUrl());
        configureAuditTable();
        configureCatalog();
        refreshAudit();
        initializeMap();
        refreshDashboard();
    }

    private void initializeMap() {
        if (!AccessControl.allows(SessionContext.role(), Permission.GPS_READ)) {
            gpsMap.getEngine().loadContent("<html><body><h3>Accès GPS non autorisé.</h3></body></html>");
            return;
        }
        WebEngine engine = gpsMap.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                refreshGpsMap();
            }
        });
        engine.loadContent(mapHtml());
    }

    @FXML
    private void refreshGpsMap() {
        if (!AccessControl.allows(SessionContext.role(), Permission.GPS_READ)) {
            return;
        }
        runAsync(
                () -> api.send("GET", "/gps/all", "").body(),
                body -> {
                    JsonArray markers = gpsMarkers(body);
                    gpsMap.getEngine().executeScript("renderMarkers(" + markers + ")");
                    audit.record("GPS TEMPS RÉEL", "/gps/all", markers.size() + " positions");
                    refreshAudit();
                },
                exception -> showError(apiErrorMessage(exception)));
    }

    private void configureCatalog() {
        EndpointCatalog.all().stream()
                .filter(endpoint -> AccessControl.allows(SessionContext.role(), endpoint.permission()))
                .forEach(authorizedEndpoints::add);
        List<String> modules = authorizedEndpoints.stream()
                .map(EndpointDefinition::module)
                .distinct()
                .sorted()
                .toList();
        moduleCombo.setItems(FXCollections.observableArrayList(modules));
        moduleCombo.valueProperty().addListener((observable, oldValue, module) -> {
            List<EndpointDefinition> endpoints = authorizedEndpoints.stream()
                    .filter(endpoint -> endpoint.module().equals(module))
                    .toList();
            endpointCombo.setItems(FXCollections.observableArrayList(endpoints));
            if (!endpoints.isEmpty()) {
                endpointCombo.getSelectionModel().selectFirst();
            }
        });
        endpointCombo.valueProperty().addListener((observable, oldValue, endpoint) -> showEndpoint(endpoint));
        if (!modules.isEmpty()) {
            moduleCombo.getSelectionModel().selectFirst();
        }
    }

    private void showEndpoint(EndpointDefinition endpoint) {
        if (endpoint == null) {
            return;
        }
        pathField.setText(endpoint.path());
        bodyArea.setText(endpoint.sampleBody());
        Map<String, String> variables = new LinkedHashMap<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(endpoint.path());
        while (matcher.find()) {
            String name = matcher.group(1);
            variables.put(name, defaultVariable(name));
        }
        variablesArea.setText(GSON.toJson(variables));
    }

    @FXML
    private void executeEndpoint() {
        EndpointDefinition endpoint = endpointCombo.getValue();
        if (endpoint == null) {
            showError("Sélectionnez un endpoint.");
            return;
        }
        try {
            AccessControl.require(SessionContext.role(), endpoint.permission());
            String resolvedPath = resolvePath(pathField.getText(), variablesArea.getText());
            String body = bodyArea.getText();
            setBusy(true);
            runAsync(
                    () -> executeRequest(endpoint, resolvedPath, body),
                    response -> {
                        lastResponse = prettyJson(response.body());
                        responseArea.setText(lastResponse);
                        populateResultTable(response.body());
                        audit.record(endpoint.method() + " " + endpoint.name(), resolvedPath, "HTTP " + response.statusCode());
                        refreshAudit();
                    },
                    exception -> {
                        audit.record(endpoint.method() + " " + endpoint.name(), resolvedPath, "ERREUR");
                        refreshAudit();
                        showError(apiErrorMessage(exception));
                    });
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private ApiResponse executeRequest(EndpointDefinition endpoint, String resolvedPath, String body) {
        if (!endpoint.contentType().startsWith("multipart/form-data")) {
            return api.send(endpoint.method(), resolvedPath, body, endpoint.contentType());
        }
        JsonObject multipart = JsonParser.parseString(body).getAsJsonObject();
        String typeFile = multipart.get("typeFile").getAsString();
        Path file = Path.of(multipart.get("filePath").getAsString());
        return api.sendMultipart(endpoint.method(), resolvedPath, Map.of("typeFile", typeFile), file);
    }

    @FXML
    private void refreshDashboard() {
        setBusy(true);
        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() {
                int users = countArray("/users", Permission.USERS_READ);
                String operationsBody = fetchBody("/operations", Permission.OPERATIONS_READ);
                int operations = arraySize(operationsBody);
                int kyc = countArray("/kyc/list", Permission.KYC_READ);
                return new DashboardData(users, operations, kyc, operationsBody);
            }
        };
        task.setOnSucceeded(event -> {
            DashboardData data = task.getValue();
            usersKpi.setText(String.valueOf(data.users()));
            operationsKpi.setText(String.valueOf(data.operations()));
            kycKpi.setText(data.kyc() < 0 ? "N/A" : String.valueOf(data.kyc()));
            apiStatusKpi.setText("Connectée");
            renderOperationChart(data.operationsBody());
            setBusy(false);
        });
        task.setOnFailed(event -> {
            apiStatusKpi.setText("Indisponible");
            setBusy(false);
            showError(apiErrorMessage(task.getException()));
        });
        Thread thread = new Thread(task, "dbr-dashboard-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private int countArray(String path, Permission permission) {
        String body = fetchBody(path, permission);
        return body.isEmpty() ? -1 : arraySize(body);
    }

    private String fetchBody(String path, Permission permission) {
        if (!AccessControl.allows(SessionContext.role(), permission)) {
            return "";
        }
        return api.send("GET", path, "").body();
    }

    private void renderOperationChart(String body) {
        operationChart.getData().clear();
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(body);
            if (!root.isJsonArray()) {
                return;
            }
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                String type = firstString(object, "typeOperation", "type", "operationType");
                counts.merge(type.isBlank() ? "AUTRE" : type.toUpperCase(), 1, Integer::sum);
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Opérations");
            counts.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
            operationChart.getData().add(series);
        } catch (JsonParseException ignored) {
            // The raw response remains visible through the endpoint console.
        }
    }

    private void populateResultTable(String body) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        try {
            JsonElement root = JsonParser.parseString(body);
            List<JsonObject> objects = new ArrayList<>();
            if (root.isJsonArray()) {
                root.getAsJsonArray().forEach(item -> {
                    if (item.isJsonObject()) {
                        objects.add(item.getAsJsonObject());
                    }
                });
            } else if (root.isJsonObject()) {
                objects.add(root.getAsJsonObject());
            }
            if (objects.isEmpty()) {
                return;
            }
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            objects.forEach(object -> object.keySet().forEach(keys::add));
            for (String key : keys) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
                column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrDefault(key, "")));
                column.setPrefWidth(150);
                resultTable.getColumns().add(column);
            }
            for (JsonObject object : objects) {
                Map<String, String> row = new LinkedHashMap<>();
                keys.forEach(key -> row.put(key, compact(object.get(key))));
                resultTable.getItems().add(row);
            }
        } catch (JsonParseException ignored) {
            // Non-JSON payloads are shown in the raw response area.
        }
    }

    @FXML
    private void exportResponse() {
        if (lastResponse.isBlank()) {
            showError("Aucune réponse à exporter.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter le rapport");
        chooser.setInitialFileName("dbr-report-" + LocalDateTime.now().toLocalDate() + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File target = chooser.showSaveDialog(responseArea.getScene().getWindow());
        if (target == null) {
            return;
        }
        try {
            Files.writeString(target.toPath(), toCsv(lastResponse), StandardCharsets.UTF_8);
            audit.record("EXPORT CSV", target.getName(), "OK");
            refreshAudit();
        } catch (IOException exception) {
            showError("Export impossible: " + exception.getMessage());
        }
    }

    @FXML
    private void saveConfiguration() {
        try {
            AccessControl.require(SessionContext.role(), Permission.CONFIG_WRITE);
            AppConfig.saveBaseUrl(baseUrlField.getText());
            audit.record("CONFIGURATION API", AppConfig.baseUrl(), "OK");
            refreshAudit();
            apiStatusKpi.setText("À vérifier");
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void logout() {
        setBusy(true);
        runAsync(
                () -> {
                    api.logout();
                    return null;
                },
                ignored -> openLogin(),
                exception -> openLogin());
    }

    private void openLogin() {
        SessionContext.clear();
        try {
            Parent root = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml")).load();
            Stage stage = (Stage) identityLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 740, 400));
            stage.centerOnScreen();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void configureAuditTable() {
        auditDate.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().timestamp()));
        auditUser.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().username()));
        auditAction.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().action()));
        auditTarget.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().target()));
        auditResult.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().result()));
    }

    private void refreshAudit() {
        if (auditTable != null) {
            auditTable.setItems(FXCollections.observableArrayList(audit.entries()));
        }
    }

    private String resolvePath(String path, String variablesJson) {
        Map<String, String> values = new LinkedHashMap<>();
        if (variablesJson != null && !variablesJson.isBlank()) {
            JsonObject object = JsonParser.parseString(variablesJson).getAsJsonObject();
            object.entrySet().forEach(entry -> values.put(entry.getKey(), entry.getValue().getAsString()));
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(path);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String value = values.get(matcher.group(1));
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Valeur manquante: " + matcher.group(1));
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private <T> void runAsync(Work<T> work, java.util.function.Consumer<T> success,
                              java.util.function.Consumer<Throwable> failure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.run();
            }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            setBusy(false);
            failure.accept(task.getException());
        });
        Thread thread = new Thread(task, "dbr-api-call");
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setBusy(busy));
            return;
        }
        progress.setVisible(busy);
        executeButton.setDisable(busy);
    }

    private static int arraySize(String body) {
        try {
            JsonElement element = JsonParser.parseString(body);
            return element.isJsonArray() ? element.getAsJsonArray().size() : 1;
        } catch (JsonParseException exception) {
            return 0;
        }
    }

    private static String prettyJson(String body) {
        try {
            return GSON.toJson(JsonParser.parseString(body));
        } catch (JsonParseException exception) {
            return body;
        }
    }

    private static String compact(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "";
        }
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        return value.toString();
    }

    private static String firstString(JsonObject object, String... names) {
        for (String name : names) {
            JsonElement value = object.get(name);
            if (value != null && value.isJsonPrimitive()) {
                return value.getAsString();
            }
        }
        return "";
    }

    private static String defaultVariable(String name) {
        return switch (name) {
            case "start" -> "2026-06-01T00:00:00";
            case "end" -> "2026-06-30T23:59:59";
            case "username" -> "+243810000001";
            case "operation_ref" -> "OP-REFERENCE";
            case "jeton_cash_reference" -> "JC-REFERENCE";
            default -> "1";
        };
    }

    private static String apiErrorMessage(Throwable throwable) {
        if (throwable instanceof ApiException apiException) {
            String detail = apiException.getResponseBody();
            return apiException.getMessage() + (detail == null || detail.isBlank() ? "" : "\n" + detail);
        }
        return throwable == null ? "Erreur inconnue." : throwable.getMessage();
    }

    private static String toCsv(String json) {
        JsonElement root = JsonParser.parseString(json);
        JsonArray array = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
        if (!root.isJsonArray()) {
            array.add(root);
        }
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        array.forEach(item -> {
            if (item.isJsonObject()) {
                headers.addAll(item.getAsJsonObject().keySet());
            }
        });
        StringBuilder csv = new StringBuilder(String.join(",", headers)).append('\n');
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            csv.append(headers.stream()
                    .map(header -> csvEscape(compact(object.get(header))))
                    .reduce((left, right) -> left + "," + right)
                    .orElse(""))
                    .append('\n');
        }
        return csv.toString();
    }

    private static String csvEscape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static JsonArray gpsMarkers(String body) {
        JsonArray markers = new JsonArray();
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonArray()) {
            return markers;
        }
        for (JsonElement item : root.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject source = item.getAsJsonObject();
            Double latitude = number(source, "latitude", "lat");
            Double longitude = number(source, "longitude", "lng", "lon");
            if (latitude == null || longitude == null) {
                continue;
            }
            JsonObject marker = new JsonObject();
            marker.addProperty("lat", latitude);
            marker.addProperty("lng", longitude);
            marker.addProperty("role", nestedString(source, "role", "type"));
            marker.addProperty("label", nestedString(source, "phone", "username", "surname", "firstname"));
            markers.add(marker);
        }
        return markers;
    }

    private static Double number(JsonObject object, String... names) {
        for (String name : names) {
            JsonElement value = object.get(name);
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                return value.getAsDouble();
            }
        }
        return null;
    }

    private static String nestedString(JsonElement element, String... names) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String name : names) {
                JsonElement value = object.get(name);
                if (value != null && value.isJsonPrimitive()) {
                    return value.getAsString();
                }
                if (value != null && value.isJsonObject() && value.getAsJsonObject().has("libelle")) {
                    return value.getAsJsonObject().get("libelle").getAsString();
                }
            }
            for (String key : object.keySet()) {
                String nested = nestedString(object.get(key), names);
                if (!nested.isBlank()) {
                    return nested;
                }
            }
        }
        return "";
    }

    private static String mapHtml() {
        return """
                <!doctype html>
                <html><head><meta charset="utf-8">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.css">
                <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.Default.css">
                <style>html,body,#map{height:100%;margin:0} .legend{background:white;padding:8px}</style>
                </head><body><div id="map"></div>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <script src="https://unpkg.com/leaflet.markercluster@1.5.3/dist/leaflet.markercluster.js"></script>
                <script>
                const map=L.map('map').setView([-4.325,15.3222],11);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                  {maxZoom:19,attribution:'OpenStreetMap'}).addTo(map);
                let cluster=L.markerClusterGroup(); map.addLayer(cluster);
                function color(role){role=(role||'').toUpperCase();
                  if(role.includes('CLIENT'))return '#16a34a';
                  if(role.includes('AGENT'))return '#2563eb';
                  if(role.includes('ADMIN')||role.includes('OFFICE'))return '#dc2626';
                  return '#7c3aed';}
                function renderMarkers(items){
                  cluster.clearLayers();
                  items.forEach(p=>{
                    const icon=L.divIcon({className:'',html:'<div style="width:16px;height:16px;border-radius:50%;background:'+color(p.role)+';border:2px solid white;box-shadow:0 1px 4px #333"></div>'});
                    cluster.addLayer(L.marker([p.lat,p.lng],{icon}).bindPopup('<b>'+(p.label||'Utilisateur')+'</b><br>'+(p.role||'Rôle inconnu')));
                  });
                  if(cluster.getLayers().length)map.fitBounds(cluster.getBounds(),{padding:[30,30],maxZoom:15});
                }
                </script></body></html>
                """;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("DBR Office");
        alert.setHeaderText("L'action n'a pas abouti");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface Work<T> {
        T run();
    }

    private record DashboardData(int users, int operations, int kyc, String operationsBody) {
    }
}
