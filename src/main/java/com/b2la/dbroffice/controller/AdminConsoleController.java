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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminConsoleController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    @FXML private Label identityLabel;
    @FXML private Label environmentLabel;
    @FXML private Label liquidityKpi;
    @FXML private Label complianceKpi;
    @FXML private Label volumeKpi;
    @FXML private Label ticketsKpi;
    @FXML private Label apiStatusKpi;
    @FXML private Label usersOverviewKpi;
    @FXML private Label accountsOverviewKpi;
    @FXML private Label operationsOverviewKpi;
    @FXML private Label conversionsOverviewKpi;
    @FXML private Label establishmentsOverviewKpi;
    @FXML private Label cashTokensOverviewKpi;
    @FXML private Label previewTitleLabel;
    @FXML private Label previewModuleLabel;
    @FXML private Label previewMethodLabel;
    @FXML private Label previewImpactLabel;
    @FXML private Tab servicesTab;
    @FXML private ComboBox<String> moduleCombo;
    @FXML private ComboBox<EndpointDefinition> endpointCombo;
    @FXML private TextField pathField;
    @FXML private TextArea variablesArea;
    @FXML private TextArea bodyArea;
    @FXML private TextArea responseArea;
    @FXML private Label actionDescriptionLabel;
    @FXML private Label resultSummaryLabel;
    @FXML private Label resultCountLabel;
    @FXML private VBox actionForm;
    @FXML private TextField resultFilterField;
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
    @FXML private Label gpsStatusLabel;
    @FXML private Label gpsCountLabel;
    @FXML private HBox recordActions;
    @FXML private Button editRecordButton;
    @FXML private Button activateUserButton;
    @FXML private Button deactivateUserButton;
    @FXML private Button deleteRecordButton;

    private final ApiClient api = ApiClient.getInstance();
    private final AuditService audit = AuditService.getInstance();
    private final List<EndpointDefinition> authorizedEndpoints = new ArrayList<>();
    private final ObservableList<Map<String, String>> resultRows = FXCollections.observableArrayList();
    private final Map<String, TextField> variableFields = new LinkedHashMap<>();
    private final List<FormBinding> formBindings = new ArrayList<>();
    private JsonObject formBody;
    private String lastResponse = "";

    @FXML
    public void initialize() {
        identityLabel.setText(SessionContext.username() + "  |  " + friendlyRole());
        baseUrlField.setText(AppConfig.baseUrl());
        environmentLabel.setText(isLocalEnvironment() ? "LOCAL" : "PRODUCTION");
        configureAuditTable();
        configureCatalog();
        refreshAudit();
        initializeMap();
        configureRecordActions();
        refreshDashboard();
    }

    private void configureRecordActions() {
        recordActions.setVisible(false);
        recordActions.setManaged(false);
        resultTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> updateRecordActions());
    }

    private void initializeMap() {
        if (!AccessControl.allows(SessionContext.role(), Permission.GPS_READ)) {
            gpsMap.getEngine().loadContent("<html><body><h3>Accès GPS non autorisé.</h3></body></html>");
            gpsStatusLabel.setText("Accès non autorisé");
            return;
        }
        gpsMap.setContextMenuEnabled(false);
        WebEngine engine = gpsMap.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                gpsStatusLabel.setText("Carte prête");
                refreshGpsMap();
            } else if (state == javafx.concurrent.Worker.State.FAILED) {
                gpsStatusLabel.setText("Chargement de la carte impossible");
            }
        });
        engine.loadContent(mapHtml());
    }

    @FXML
    private void refreshGpsMap() {
        if (!AccessControl.allows(SessionContext.role(), Permission.GPS_READ)) {
            return;
        }
        gpsStatusLabel.setText("Actualisation des positions...");
        runAsync(
                () -> api.send("GET", "/gps/all", "").body(),
                body -> {
                    JsonArray markers = gpsMarkers(body);
                    try {
                        gpsMap.getEngine().executeScript("window.setMarkers(" + markers + ")");
                    } catch (RuntimeException exception) {
                        gpsStatusLabel.setText("Carte indisponible, données reçues");
                        return;
                    }
                    gpsCountLabel.setText(markers.size() + (markers.size() > 1 ? " positions" : " position"));
                    gpsStatusLabel.setText(markers.isEmpty()
                            ? "Aucune position GPS enregistrée"
                            : "Positions mises à jour");
                    audit.record("GPS TEMPS RÉEL", "/gps/all", markers.size() + " positions");
                    refreshAudit();
                },
                exception -> {
                    gpsStatusLabel.setText("Impossible de récupérer les positions");
                    showError(apiErrorMessage(exception));
                });
    }

    private static boolean isBackOfficeEndpoint(EndpointDefinition endpoint) {
        String module = endpoint.module();
        String method = endpoint.method();
        String path = endpoint.path();

        if ("Authentification".equals(module)) {
            return false;
        }
        if ("Conversions".equals(module)) {
            return "GET".equals(method) && "/conversions".equals(path);
        }
        if (Set.of("Opérations", "Jetons cash", "GPS").contains(module)) {
            return "GET".equals(method);
        }
        if ("Services client".equals(module)) {
            return "GET".equals(method)
                    && Set.of("/services", "/services/{id}", "/document-types", "/convrtiseurs").contains(path);
        }
        return !("KYC".equals(module) && "POST".equals(method));
    }

    private static boolean isLocalEnvironment() {
        String baseUrl = AppConfig.baseUrl().toLowerCase(Locale.ROOT);
        return baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
    }

    private void configureCatalog() {
        EndpointCatalog.all().stream()
                .filter(endpoint -> !endpoint.path().equals("/auth/login"))
                .filter(AdminConsoleController::isBackOfficeEndpoint)
                .filter(endpoint -> AccessControl.allows(SessionContext.role(), endpoint.permission()))
                .forEach(authorizedEndpoints::add);
        List<String> modules = authorizedEndpoints.stream()
                .map(EndpointDefinition::module)
                .distinct()
                .sorted()
                .toList();
        moduleCombo.setItems(FXCollections.observableArrayList(modules));
        moduleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(String module) {
                return module == null ? "" : friendlyModuleName(module);
            }

            @Override
            public String fromString(String value) {
                return value;
            }
        });
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
        actionDescriptionLabel.setText(actionDescription(endpoint));
        executeButton.setText(actionButtonLabel(endpoint));
        previewTitleLabel.setText(endpoint.name());
        previewModuleLabel.setText(friendlyModuleName(endpoint.module()));
        previewMethodLabel.setText(friendlyMethod(endpoint.method()));
        previewImpactLabel.setText(actionImpact(endpoint));
        buildActionForm(endpoint, variables);
        updateRecordActions();
    }

    private void updateRecordActions() {
        EndpointDefinition current = endpointCombo.getValue();
        boolean hasSelection = resultTable.getSelectionModel().getSelectedItem() != null;
        boolean readAction = current != null && "GET".equals(current.method());
        String module = current == null ? "" : current.module();
        boolean userModule = "Utilisateurs".equals(module);
        boolean editable = hasSelection && readAction && authorizedEndpoints.stream()
                .anyMatch(endpoint -> endpoint.module().equals(module)
                        && endpoint.name().startsWith("Modifier"));
        boolean deletable = hasSelection && readAction && authorizedEndpoints.stream()
                .anyMatch(endpoint -> endpoint.module().equals(module)
                        && "DELETE".equals(endpoint.method()));
        boolean visible = hasSelection && readAction && (editable || deletable || userModule);

        recordActions.setVisible(visible);
        recordActions.setManaged(visible);
        editRecordButton.setVisible(editable);
        editRecordButton.setManaged(editable);
        deleteRecordButton.setVisible(deletable);
        deleteRecordButton.setManaged(deletable);
        activateUserButton.setVisible(userModule);
        activateUserButton.setManaged(userModule);
        deactivateUserButton.setVisible(userModule);
        deactivateUserButton.setManaged(userModule);
    }

    @FXML
    private void openUsersOverview() {
        openBusinessAction("Utilisateurs", "Lister les utilisateurs");
    }

    @FXML
    private void openAccountsOverview() {
        openBusinessAction("Comptes", "Lister les comptes");
    }

    @FXML
    private void openOperationsOverview() {
        openBusinessAction("Opérations", "Lister les opérations");
    }

    @FXML
    private void openConversionsOverview() {
        openBusinessAction("Conversions", "Lister les conversions");
    }

    @FXML
    private void openBillingOverview() {
        Optional<EndpointDefinition> action = findBusinessAction("Services client", "Lister les services")
                .or(() -> findBusinessAction("Établissements", "Lister les établissements"));
        if (action.isPresent()) {
            selectBusinessAction(action.get());
        } else {
            showError("Les services de facturation ne sont pas disponibles pour votre rôle.");
        }
    }

    @FXML
    private void openCashTokensOverview() {
        Optional<EndpointDefinition> action = findBusinessAction("Jetons cash", "Créer un jeton")
                .or(() -> findBusinessAction("Jetons cash", "Historique des jetons"));
        if (action.isPresent()) {
            selectBusinessAction(action.get());
        } else {
            showError("Le service Jeton Cash n'est pas disponible pour votre rôle.");
        }
    }

    @FXML
    private void openKycOverview() {
        openBusinessAction("KYC", "Lister les KYC");
    }

    private boolean openBusinessAction(String module, String actionName) {
        Optional<EndpointDefinition> match = findBusinessAction(module, actionName);
        if (match.isEmpty()) {
            showError("Cette action n'est pas disponible pour votre rôle.");
            return false;
        }
        selectBusinessAction(match.get());
        return true;
    }

    private Optional<EndpointDefinition> findBusinessAction(String module, String actionName) {
        return authorizedEndpoints.stream()
                .filter(endpoint -> endpoint.module().equals(module))
                .filter(endpoint -> endpoint.name().equals(actionName))
                .findFirst();
    }

    private void selectBusinessAction(EndpointDefinition endpoint) {
        if (servicesTab != null && servicesTab.getTabPane() != null) {
            servicesTab.getTabPane().getSelectionModel().select(servicesTab);
        }
        moduleCombo.getSelectionModel().select(endpoint.module());
        endpointCombo.getSelectionModel().select(endpoint);
    }

    @FXML
    private void clearActionForm() {
        showEndpoint(endpointCombo.getValue());
    }

    private void buildActionForm(EndpointDefinition endpoint, Map<String, String> variables) {
        actionForm.getChildren().clear();
        variableFields.clear();
        formBindings.clear();
        formBody = null;

        variables.forEach((name, value) -> {
            TextField field = formField(value);
            variableFields.put(name, field);
            addFormControl(friendlyFieldName(name), field);
        });

        if (endpoint.sampleBody() != null && !endpoint.sampleBody().isBlank()) {
            try {
                JsonElement sample = JsonParser.parseString(endpoint.sampleBody());
                if (sample.isJsonObject()) {
                    formBody = sample.getAsJsonObject();
                    addJsonFields(formBody);
                }
            } catch (JsonParseException ignored) {
                // Les données restent disponibles dans les options techniques.
            }
        }

        if (actionForm.getChildren().isEmpty()) {
            Label noInput = new Label("Aucune information supplémentaire n'est nécessaire.");
            noInput.getStyleClass().add("help-text");
            actionForm.getChildren().add(noInput);
        }
    }

    private void addJsonFields(JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonObject()) {
                addJsonFields(value.getAsJsonObject());
                continue;
            }
            Control control;
            if (value != null && (value.isJsonArray() || value.isJsonObject())) {
                TextArea area = new TextArea(GSON.toJson(value));
                area.setPrefRowCount(3);
                control = area;
            } else {
                control = typedControl(entry.getKey(), value);
            }
            formBindings.add(new FormBinding(object, entry.getKey(), control, value));
            addFormControl(friendlyFieldName(entry.getKey()), control);
        }
    }

    private Control typedControl(String name, JsonElement value) {
        String normalized = name.toLowerCase(Locale.ROOT);
        String current = value == null || value.isJsonNull() ? "" : value.getAsString();
        if (normalized.contains("datebirth") || normalized.equals("date") || normalized.endsWith("date")) {
            DatePicker picker = new DatePicker();
            try {
                picker.setValue(current.isBlank() ? null : LocalDate.parse(current.substring(0, Math.min(10, current.length()))));
            } catch (RuntimeException ignored) {
                picker.getEditor().setText(current);
            }
            return picker;
        }
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()
                || normalized.equals("active") || normalized.equals("state")) {
            CheckBox checkBox = new CheckBox(current.isBlank() ? "Oui" : Boolean.parseBoolean(current) ? "Actif" : "Inactif");
            checkBox.setSelected(Boolean.parseBoolean(current));
            checkBox.selectedProperty().addListener((obs, oldValue, selected) ->
                    checkBox.setText(selected ? "Actif" : "Inactif"));
            return checkBox;
        }
        List<String> choices = choicesFor(normalized);
        if (!choices.isEmpty()) {
            ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(choices));
            combo.setEditable(true);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setValue(current);
            return combo;
        }
        TextField field = formField(current);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                || normalized.contains("amount") || normalized.equals("min") || normalized.equals("max")
                || normalized.contains("percent") || normalized.contains("taux")) {
            field.setPromptText("0,00");
            field.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("-?\\d*([.,]\\d{0,4})?") ? change : null));
        } else if (normalized.contains("phone")) {
            field.setPromptText("+243...");
        } else if (normalized.contains("email")) {
            field.setPromptText("nom@domaine.com");
        } else if (normalized.contains("password") || normalized.equals("clef") || normalized.equals("code")) {
            field.setPromptText("Valeur sécurisée");
        }
        return field;
    }

    private static List<String> choicesFor(String name) {
        if (name.equals("devise") || name.equals("etranger") || name.contains("currency")) {
            return List.of("CDF", "USD", "EUR");
        }
        if (name.equals("typeoperation")) {
            return List.of("TRANSFERT", "DEPOT", "RETRAIT", "PAIEMENT", "CONVERSION");
        }
        if (name.equals("statecarte")) {
            return List.of("VALIDER", "REFUSER", "EN_ATTENTE");
        }
        if (name.equals("typefile")) {
            return List.of("PASSPORT", "CARTE_IDENTITE", "PERMIS_CONDUIRE");
        }
        if (name.equals("libelle") || name.equals("role")) {
            return List.of("CLIENT", "AGENT", "OFFICE", "ADMIN", "ENTITES");
        }
        return List.of();
    }

    private TextField formField(String value) {
        TextField field = new TextField(value);
        field.getStyleClass().add("form-field");
        return field;
    }

    private void addFormControl(String labelText, Control control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        actionForm.getChildren().addAll(label, control);
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
            if ("DELETE".equals(endpoint.method()) && !confirmDeletion(endpoint.name())) {
                return;
            }
            synchronizeAdministrativeForm();
            String resolvedPath = resolvePath(pathField.getText(), variablesArea.getText());
            String body = bodyArea.getText();
            setBusy(true);
            runAsync(
                    () -> executeRequest(endpoint, resolvedPath, body),
                    response -> {
                        lastResponse = prettyJson(response.body());
                        responseArea.setText(lastResponse);
                        populateResultTable(response.body());
                        resultSummaryLabel.setText("Action terminée avec succès.");
                        audit.record(endpoint.method() + " " + endpoint.name(), resolvedPath, "HTTP " + response.statusCode());
                        refreshAudit();
                    },
                    exception -> {
                        audit.record(endpoint.method() + " " + endpoint.name(), resolvedPath, "ERREUR");
                        refreshAudit();
                        resultSummaryLabel.setText("L'action n'a pas pu être terminée.");
                        showError(apiErrorMessage(exception));
                    });
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void synchronizeAdministrativeForm() {
        if (!variableFields.isEmpty()) {
            Map<String, String> variables = new LinkedHashMap<>();
            variableFields.forEach((name, field) -> variables.put(name, field.getText().trim()));
            variablesArea.setText(GSON.toJson(variables));
        }
        for (FormBinding binding : formBindings) {
            binding.parent().add(binding.key(), convertedValue(controlValue(binding.control()), binding.original()));
        }
        if (formBody != null) {
            bodyArea.setText(GSON.toJson(formBody));
        }
    }

    private static String controlValue(Control control) {
        if (control instanceof TextInputControl input) {
            return input.getText().trim().replace(',', '.');
        }
        if (control instanceof DatePicker picker) {
            return picker.getValue() == null ? picker.getEditor().getText().trim() : picker.getValue().toString();
        }
        if (control instanceof CheckBox checkBox) {
            return Boolean.toString(checkBox.isSelected());
        }
        if (control instanceof ComboBox<?> comboBox) {
            Object value = comboBox.getValue();
            return value == null ? "" : value.toString().trim();
        }
        return "";
    }

    @FXML
    private void editSelectedRecord() {
        Map<String, String> selected = selectedResult();
        if (selected == null) return;
        String module = endpointCombo.getValue() == null ? moduleCombo.getValue() : endpointCombo.getValue().module();
        Optional<EndpointDefinition> action = authorizedEndpoints.stream()
                .filter(endpoint -> endpoint.module().equals(module))
                .filter(endpoint -> endpoint.name().startsWith("Modifier"))
                .findFirst();
        if (action.isEmpty()) {
            showError("Aucune action de modification n'est exposée par le backend pour ce type de donnée.");
            return;
        }
        selectBusinessAction(action.get());
        fillFormFromSelection(selected);
    }

    @FXML
    private void activateSelectedUser() {
        prepareUserStateAction(true);
    }

    @FXML
    private void deactivateSelectedUser() {
        prepareUserStateAction(false);
    }

    private void prepareUserStateAction(boolean active) {
        Map<String, String> selected = selectedResult();
        if (selected == null) return;
        Optional<EndpointDefinition> action = findBusinessAction("Utilisateurs", "Activer ou désactiver un utilisateur");
        if (action.isEmpty()) {
            showError("Le backend ne fournit pas encore cette action.");
            return;
        }
        selectBusinessAction(action.get());
        String id = selectedId(selected);
        variableFields.values().stream().findFirst().ifPresent(field -> field.setText(id));
        formBindings.stream()
                .filter(binding -> binding.key().equals("active") && binding.control() instanceof CheckBox)
                .map(binding -> (CheckBox) binding.control())
                .forEach(checkBox -> checkBox.setSelected(active));
    }

    @FXML
    private void deleteSelectedRecord() {
        Map<String, String> selected = selectedResult();
        if (selected == null) return;
        String module = endpointCombo.getValue() == null ? moduleCombo.getValue() : endpointCombo.getValue().module();
        Optional<EndpointDefinition> action = authorizedEndpoints.stream()
                .filter(endpoint -> endpoint.module().equals(module))
                .filter(endpoint -> endpoint.method().equals("DELETE"))
                .findFirst();
        if (action.isEmpty()) {
            showError("La suppression n'est pas autorisée pour cette donnée.");
            return;
        }
        selectBusinessAction(action.get());
        String id = selectedId(selected);
        variableFields.values().stream().findFirst().ifPresent(field -> field.setText(id));
    }

    private Map<String, String> selectedResult() {
        Map<String, String> selected = resultTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez d'abord une ligne du tableau.");
        }
        return selected;
    }

    private void fillFormFromSelection(Map<String, String> selected) {
        String id = selectedId(selected);
        variableFields.values().stream().findFirst().ifPresent(field -> field.setText(id));
        for (FormBinding binding : formBindings) {
            String value = selected.getOrDefault(binding.key(), selected.getOrDefault("response." + binding.key(), ""));
            if (value.isBlank()) continue;
            if (binding.control() instanceof TextInputControl input) input.setText(value);
            else if (binding.control() instanceof ComboBox<?> combo) ((ComboBox<String>) combo).setValue(value);
            else if (binding.control() instanceof CheckBox checkBox) checkBox.setSelected(Boolean.parseBoolean(value));
            else if (binding.control() instanceof DatePicker picker) {
                try { picker.setValue(LocalDate.parse(value.substring(0, Math.min(10, value.length())))); }
                catch (RuntimeException ignored) { picker.getEditor().setText(value); }
            }
        }
    }

    private static String selectedId(Map<String, String> selected) {
        for (String key : List.of("id", "Id", "response.id", "reference", "codeReference")) {
            String value = selected.get(key);
            if (value != null && !value.isBlank()) return value;
        }
        throw new IllegalArgumentException("Cette ligne ne contient aucun identifiant exploitable.");
    }

    private static JsonElement convertedValue(String text, JsonElement original) {
        if (original == null || original.isJsonNull()) {
            return text.isBlank() ? JsonNull.INSTANCE : new JsonPrimitive(text);
        }
        if (original.isJsonPrimitive()) {
            JsonPrimitive primitive = original.getAsJsonPrimitive();
            try {
                if (primitive.isBoolean()) {
                    return new JsonPrimitive(Boolean.parseBoolean(text));
                }
                if (primitive.isNumber()) {
                    return new JsonPrimitive(new BigDecimal(text));
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Une valeur numérique est incorrecte: " + text);
            }
            return new JsonPrimitive(text);
        }
        try {
            return JsonParser.parseString(text);
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("Une liste ou donnée structurée est incorrecte.");
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
                String accountsBody = fetchBody("/accounts", Permission.DASHBOARD_READ);
                String operationsBody = fetchBody("/operations", Permission.OPERATIONS_READ);
                String kycBody = fetchOptionalBody("/kyc/list", Permission.KYC_READ);
                String messagesBody = fetchOptionalBody("/messagerie", Permission.MESSAGING_READ);
                String usersBody = fetchOptionalBody("/users", Permission.USERS_READ);
                String conversionsBody = fetchOptionalBody("/conversions", Permission.OPERATIONS_READ);
                String establishmentsBody = fetchOptionalBody("/etablissements", Permission.DASHBOARD_READ);
                String cashTokensBody = fetchOptionalBody("/jetons-cash", Permission.OPERATIONS_READ);
                return new DashboardData(
                        totalsByCurrency(accountsBody),
                        pendingKycCount(kycBody),
                        volumeLast24Hours(operationsBody),
                        unreadMessageCount(messagesBody),
                        itemCount(usersBody),
                        itemCount(accountsBody),
                        itemCount(operationsBody),
                        itemCount(conversionsBody),
                        itemCount(establishmentsBody),
                        itemCount(cashTokensBody),
                        operationsBody);
            }
        };
        task.setOnSucceeded(event -> {
            DashboardData data = task.getValue();
            liquidityKpi.setText(data.liquidity());
            complianceKpi.setText(displayCount(data.pendingKyc()));
            volumeKpi.setText(data.volume24h());
            ticketsKpi.setText(displayCount(data.unreadMessages()));
            usersOverviewKpi.setText(displayCount(data.users()));
            accountsOverviewKpi.setText(displayCount(data.accounts()));
            operationsOverviewKpi.setText(displayCount(data.operations()));
            conversionsOverviewKpi.setText(displayCount(data.conversions()));
            establishmentsOverviewKpi.setText(displayCount(data.establishments()));
            cashTokensOverviewKpi.setText(displayCount(data.cashTokens()));
            apiStatusKpi.setText("Opérationnel");
            renderOperationChart(data.operationsBody());
            setBusy(false);
        });
        task.setOnFailed(event -> {
            apiStatusKpi.setText("Hors service");
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

    private String fetchOptionalBody(String path, Permission permission) {
        if (!AccessControl.allows(SessionContext.role(), permission)) {
            return "";
        }
        try {
            return api.send("GET", path, "").body();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private void renderOperationChart(String body) {
        operationChart.getData().clear();
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            JsonElement root = businessPayload(JsonParser.parseString(body));
            if (!root.isJsonArray()) {
                return;
            }
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                String status = nestedString(object.get("state"), "libelle", "status", "state");
                counts.merge(friendlyOperationStatus(status), 1, Integer::sum);
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
        resultRows.clear();
        resultFilterField.clear();
        try {
            JsonElement root = displayPayload(JsonParser.parseString(body));
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
            List<Map<String, String>> flattenedRows = objects.stream()
                    .map(AdminConsoleController::flattenObject)
                    .toList();
            flattenedRows.forEach(row -> keys.addAll(row.keySet()));
            for (String key : keys) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(friendlyColumnName(key));
                column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrDefault(key, "")));
                column.setPrefWidth(preferredColumnWidth(key));
                resultTable.getColumns().add(column);
            }
            resultRows.setAll(flattenedRows);
            resultTable.setItems(resultRows);
            updateResultCount(resultRows.size());
            updateRecordActions();
        } catch (JsonParseException ignored) {
            // Non-JSON payloads are shown in the raw response area.
            updateResultCount(0);
        }
    }

    @FXML
    private void filterResults() {
        String query = resultFilterField.getText() == null
                ? ""
                : resultFilterField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isBlank()) {
            resultTable.setItems(resultRows);
            updateResultCount(resultRows.size());
            return;
        }
        ObservableList<Map<String, String>> filtered = resultRows.filtered(row ->
                row.values().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(query)));
        resultTable.setItems(filtered);
        updateResultCount(filtered.size());
    }

    private void updateResultCount(int count) {
        resultCountLabel.setText(count + (count > 1 ? " résultats" : " résultat"));
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
            stage.setScene(new Scene(root, 920, 560));
            stage.centerOnScreen();
            stage.setTitle("DBR Core Finance");
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
            JsonElement element = businessPayload(JsonParser.parseString(body));
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

    private static JsonElement businessPayload(JsonElement root) {
        JsonElement current = root;
        if (current != null && current.isJsonObject()) {
            JsonObject object = current.getAsJsonObject();
            if (object.has("data") && !object.get("data").isJsonNull()) {
                current = object.get("data");
            }
        }
        if (current != null && current.isJsonObject()) {
            JsonObject object = current.getAsJsonObject();
            if (object.has("response") && !object.get("response").isJsonNull()) {
                current = object.get("response");
            }
        }
        return current == null ? JsonNull.INSTANCE : current;
    }

    private static JsonElement displayPayload(JsonElement root) {
        JsonElement payload = businessPayload(root);
        if (!payload.isJsonObject()) {
            return payload;
        }
        JsonObject object = payload.getAsJsonObject();
        for (String key : List.of("acctList", "operations", "users", "kycs", "messages", "items", "content")) {
            JsonElement value = object.get(key);
            if (value != null && value.isJsonArray()) {
                return value;
            }
        }
        return payload;
    }

    private static Map<String, String> flattenObject(JsonObject object) {
        Map<String, String> row = new LinkedHashMap<>();
        flattenInto(row, "", object);
        return row;
    }

    private static void flattenInto(Map<String, String> target, String prefix, JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonObject()) {
                if ("grpHdr".equals(entry.getKey()) || "authorities".equals(entry.getKey())) {
                    continue;
                }
                flattenInto(target, key, value.getAsJsonObject());
            } else if (value != null && value.isJsonArray()) {
                target.put(key, value.getAsJsonArray().size() + " élément(s)");
            } else {
                target.put(key, compact(value));
            }
        }
    }

    private static String friendlyColumnName(String key) {
        String leaf = key.substring(key.lastIndexOf('.') + 1);
        return switch (leaf) {
            case "id", "otherId", "customerId" -> "Identifiant";
            case "name" -> "Nom";
            case "phone", "phoneNumber" -> "Téléphone";
            case "email", "emailAddress" -> "E-mail";
            case "amount" -> "Montant";
            case "currency", "signe" -> "Devise";
            case "typeCode", "libelle", "typeOperation" -> "Type";
            case "state", "stateCarte", "active" -> "Statut";
            case "codereference", "reference" -> "Référence";
            case "dateoperation", "datemessage", "uploadDate" -> "Date";
            case "message" -> "Message";
            case "customerType", "customerCategory" -> "Catégorie";
            default -> splitCamelCase(leaf);
        };
    }

    private static String splitCamelCase(String value) {
        String label = value.replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2");
        return label.isBlank()
                ? value
                : Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    private static double preferredColumnWidth(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.contains("message") || lower.contains("name")) {
            return 220;
        }
        if (lower.contains("date") || lower.contains("reference")) {
            return 180;
        }
        return 135;
    }

    private static String totalsByCurrency(String body) {
        if (body == null || body.isBlank()) {
            return "Indisponible";
        }
        try {
            JsonElement root = JsonParser.parseString(body);
            JsonArray accounts = arrayAt(root, "data", "acctList");
            Map<String, BigDecimal> totals = new TreeMap<>();
            for (JsonElement item : accounts) {
                JsonObject account = objectAt(item, "acct");
                JsonObject balance = objectAt(account, "balance");
                String currency = stringAt(balance, "currency");
                BigDecimal amount = decimalAt(balance, "amount");
                if (!currency.isBlank() && amount != null) {
                    totals.merge(currency, amount, BigDecimal::add);
                }
            }
            return formatCurrencyTotals(totals);
        } catch (JsonParseException exception) {
            return "Indisponible";
        }
    }

    private static String volumeLast24Hours(String body) {
        if (body == null || body.isBlank()) {
            return "Indisponible";
        }
        try {
            JsonElement payload = businessPayload(JsonParser.parseString(body));
            if (!payload.isJsonArray()) {
                return "0";
            }
            java.time.OffsetDateTime cutoff = java.time.OffsetDateTime.now().minusHours(24);
            Map<String, BigDecimal> totals = new TreeMap<>();
            for (JsonElement item : payload.getAsJsonArray()) {
                if (!item.isJsonObject()) {
                    continue;
                }
                JsonObject operation = item.getAsJsonObject();
                java.time.OffsetDateTime date = parseDate(firstString(operation, "dateoperation", "dateOperation"));
                if (date == null || date.isBefore(cutoff)) {
                    continue;
                }
                BigDecimal amount = decimalAt(operation, "amount");
                String currency = nestedString(operation.get("devise"), "signe", "currency");
                if (amount != null && !currency.isBlank()) {
                    totals.merge(currency, amount, BigDecimal::add);
                }
            }
            return formatCurrencyTotals(totals);
        } catch (JsonParseException exception) {
            return "Indisponible";
        }
    }

    private static int pendingKycCount(String body) {
        if (body == null || body.isBlank()) {
            return 0;
        }
        try {
            JsonElement payload = businessPayload(JsonParser.parseString(body));
            return countMatchingStatus(payload, Set.of("ATTENTE", "PENDING"));
        } catch (JsonParseException exception) {
            return -1;
        }
    }

    private static int unreadMessageCount(String body) {
        if (body == null || body.isBlank()) {
            return -1;
        }
        try {
            JsonElement payload = businessPayload(JsonParser.parseString(body));
            if (!payload.isJsonArray()) {
                return 0;
            }
            int unread = 0;
            for (JsonElement item : payload.getAsJsonArray()) {
                if (item.isJsonObject()) {
                    JsonElement state = item.getAsJsonObject().get("state");
                    if (state != null && state.isJsonPrimitive() && !state.getAsBoolean()) {
                        unread++;
                    }
                }
            }
            return unread;
        } catch (JsonParseException exception) {
            return -1;
        }
    }

    private static int itemCount(String body) {
        if (body == null || body.isBlank()) {
            return -1;
        }
        try {
            JsonElement payload = displayPayload(JsonParser.parseString(body));
            if (payload.isJsonArray()) {
                return payload.getAsJsonArray().size();
            }
            if (payload.isJsonObject()) {
                for (String key : List.of(
                        "acctList", "response", "users", "operations", "conversions",
                        "etablissements", "jetons", "items", "content")) {
                    JsonElement value = payload.getAsJsonObject().get(key);
                    if (value != null && value.isJsonArray()) {
                        return value.getAsJsonArray().size();
                    }
                }
                return 1;
            }
            return 0;
        } catch (JsonParseException exception) {
            return -1;
        }
    }

    private static int countMatchingStatus(JsonElement element, Set<String> statuses) {
        if (element == null || element.isJsonNull()) {
            return 0;
        }
        if (element.isJsonArray()) {
            int count = 0;
            for (JsonElement child : element.getAsJsonArray()) {
                count += countMatchingStatus(child, statuses);
            }
            return count;
        }
        if (!element.isJsonObject()) {
            return 0;
        }
        JsonObject object = element.getAsJsonObject();
        String status = firstString(object, "stateCarte", "status");
        if (status.isBlank()) {
            status = nestedString(object.get("state"), "libelle");
        }
        if (statuses.contains(status.toUpperCase(Locale.ROOT))) {
            return 1;
        }
        int count = 0;
        for (String key : object.keySet()) {
            count += countMatchingStatus(object.get(key), statuses);
        }
        return count;
    }

    private static JsonArray arrayAt(JsonElement element, String... path) {
        JsonElement current = element;
        for (String key : path) {
            if (current == null || !current.isJsonObject()) {
                return new JsonArray();
            }
            current = current.getAsJsonObject().get(key);
        }
        return current != null && current.isJsonArray() ? current.getAsJsonArray() : new JsonArray();
    }

    private static JsonObject objectAt(JsonElement element, String key) {
        if (element == null || !element.isJsonObject()) {
            return new JsonObject();
        }
        JsonElement value = element.getAsJsonObject().get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static String stringAt(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static BigDecimal decimalAt(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return value.getAsBigDecimal();
    }

    private static String formatCurrencyTotals(Map<String, BigDecimal> totals) {
        if (totals.isEmpty()) {
            return "0";
        }
        return totals.entrySet().stream()
                .map(entry -> entry.getValue().stripTrailingZeros().toPlainString() + " " + entry.getKey())
                .collect(java.util.stream.Collectors.joining("  |  "));
    }

    private static java.time.OffsetDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return java.time.OffsetDateTime.parse(value);
        } catch (java.time.format.DateTimeParseException exception) {
            try {
                return java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
            } catch (java.time.format.DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static String displayCount(int count) {
        return count < 0 ? "Indisponible" : String.valueOf(count);
    }

    private static String friendlyOperationStatus(String status) {
        return switch (status == null ? "" : status.toUpperCase(Locale.ROOT)) {
            case "ATTENTE", "PENDING" -> "En attente";
            case "VALIDER", "COMPLETED" -> "Validées";
            case "REFUSER", "REJECTED" -> "Refusées";
            case "ANNULER", "CANCELLED" -> "Annulées";
            default -> "Autres";
        };
    }

    private static String friendlyFieldName(String name) {
        return switch (name) {
            case "username", "phone", "phoneOwn", "phoneBeneficier" -> "Numéro de téléphone";
            case "password" -> "Mot de passe";
            case "firstname" -> "Nom";
            case "lastname" -> "Postnom";
            case "surname" -> "Prénom";
            case "email" -> "Adresse e-mail";
            case "datebirth" -> "Date de naissance";
            case "amount", "value" -> "Montant";
            case "devise", "devices" -> "Devise";
            case "typeOperation" -> "Type d'opération";
            case "operation_ref", "reference", "codeReference" -> "Référence de l'opération";
            case "clef", "code" -> "Code de confirmation";
            case "reasonCancel" -> "Motif de refus";
            case "stateCarte", "status", "state" -> "Statut";
            case "typeFile" -> "Type de document";
            case "filePath" -> "Document à transmettre";
            case "start" -> "Date de début";
            case "end" -> "Date de fin";
            case "user_id" -> "Identifiant utilisateur";
            case "account_id" -> "Identifiant du compte";
            case "kyc_id" -> "Identifiant du dossier KYC";
            case "message_id" -> "Identifiant du message";
            case "cost_id" -> "Identifiant du tarif";
            case "devise_id" -> "Identifiant de la devise";
            case "etablissement_id" -> "Identifiant de l'établissement";
            default -> name.replace('_', ' ');
        };
    }

    private static String friendlyModuleName(String module) {
        return switch (module) {
            case "Authentification" -> "Accès et mots de passe";
            case "Utilisateurs" -> "Clients et personnel";
            case "Comptes" -> "Comptes financiers";
            case "Adresses" -> "Adresses des clients";
            case "Configuration" -> "Tarifs et devises";
            case "Établissements" -> "Partenaires et établissements";
            case "Opérations" -> "Dépôts, retraits et transferts";
            case "Conversions" -> "Change de devises";
            case "Jetons cash" -> "Jetons de paiement";
            case "KYC" -> "Contrôle d'identité";
            case "GPS" -> "Présence terrain";
            case "Messagerie" -> "Messages et notifications";
            case "Reporting" -> "Rapports de gestion";
            case "Services client" -> "Services destinés aux clients";
            default -> module;
        };
    }

    private static String actionDescription(EndpointDefinition endpoint) {
        return switch (endpoint.method()) {
            case "GET" -> "Consultez " + endpoint.name().toLowerCase(Locale.ROOT) + ".";
            case "POST" -> "Enregistrez " + endpoint.name().toLowerCase(Locale.ROOT) + ". Vérifiez les informations avant de confirmer.";
            case "PUT" -> "Mettez à jour " + endpoint.name().toLowerCase(Locale.ROOT) + ".";
            case "DELETE" -> "Cette action supprime un élément. Vérifiez attentivement l'identifiant avant de confirmer.";
            default -> endpoint.name();
        };
    }

    private static String actionButtonLabel(EndpointDefinition endpoint) {
        return switch (endpoint.method()) {
            case "GET" -> "Afficher les informations";
            case "DELETE" -> "Confirmer la suppression";
            default -> "Enregistrer et confirmer";
        };
    }

    private static String friendlyMethod(String method) {
        return switch (method) {
            case "GET" -> "Consultation";
            case "POST" -> "Création / traitement";
            case "PUT" -> "Modification / validation";
            case "DELETE" -> "Suppression";
            default -> method;
        };
    }

    private static String actionImpact(EndpointDefinition endpoint) {
        return switch (endpoint.method()) {
            case "GET" -> "Lecture seule, aucune donnée modifiée";
            case "DELETE" -> "Action sensible et potentiellement définitive";
            case "PUT" -> "Met à jour une donnée ou un statut métier";
            case "POST" -> "Crée ou déclenche une nouvelle instruction";
            default -> "Action métier";
        };
    }

    private static String friendlyRole() {
        if (SessionContext.role() == null) {
            return "Utilisateur";
        }
        return switch (SessionContext.role()) {
            case SUPER_ADMIN -> "Administrateur";
            case COMPLIANCE_AGENT -> "Agent back-office";
            case ANALYST -> "Analyste";
        };
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
        JsonElement root = businessPayload(JsonParser.parseString(body));
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
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">
                <style>
                  html,body,#map{height:100%;margin:0;background:#dbe7ef}
                  #empty{display:none;position:absolute;z-index:1000;top:20px;left:50%;
                    transform:translateX(-50%);background:white;padding:10px 16px;border-radius:6px;
                    box-shadow:0 2px 10px rgba(0,0,0,.18);font-family:Arial;color:#334e68}
                  .pin{width:14px;height:14px;border-radius:50%;border:2px solid white;
                    box-shadow:0 1px 4px #334e68}
                </style>
                </head><body><div id="map"></div><div id="empty">Aucune position GPS enregistrée</div>
                <script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"></script>
                <script>
                var map=null, markers=[], pending=[];
                function color(role){role=(role||'').toUpperCase();
                  if(role.includes('CLIENT'))return '#16a34a';
                  if(role.includes('AGENT'))return '#2563eb';
                  if(role.includes('ADMIN')||role.includes('OFFICE'))return '#dc2626';
                  return '#7c3aed';}
                function initialize(){
                  if(typeof L==='undefined'){
                    document.getElementById('empty').style.display='block';
                    document.getElementById('empty').textContent='Fond de carte indisponible';
                    return;
                  }
                  map=L.map('map',{zoomControl:true}).setView([-4.325,15.3222],11);
                  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                    {maxZoom:19,attribution:'© OpenStreetMap'}).addTo(map);
                  render(pending);
                  setTimeout(function(){map.invalidateSize();},200);
                }
                function render(items){
                  pending=items||[];
                  if(!map)return;
                  markers.forEach(function(marker){map.removeLayer(marker);});
                  markers=[];
                  var empty=document.getElementById('empty');
                  empty.style.display=pending.length?'none':'block';
                  var bounds=[];
                    pending.forEach(function(p){
                    var icon=L.divIcon({className:'',html:'<div class="pin" style="background:'+color(p.role)+'"></div>'});
                    var marker=L.marker([p.lat,p.lng],{icon:icon})
                      .bindPopup('<b>'+(p.label||'Utilisateur')+'</b><br>'+(p.role||'Rôle inconnu'));
                    marker.addTo(map); markers.push(marker); bounds.push([p.lat,p.lng]);
                    });
                  if(bounds.length)map.fitBounds(bounds,{padding:[35,35],maxZoom:15});
                }
                window.setMarkers=function(items){render(items);};
                window.addEventListener('load',initialize);
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

    private boolean confirmDeletion(String actionName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation requise");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText(actionName + "\n\nCette action peut être définitive.");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    @FunctionalInterface
    private interface Work<T> {
        T run();
    }

    private record DashboardData(
            String liquidity,
            int pendingKyc,
            String volume24h,
            int unreadMessages,
            int users,
            int accounts,
            int operations,
            int conversions,
            int establishments,
            int cashTokens,
            String operationsBody) {
    }

    private record FormBinding(
            JsonObject parent, String key, Control control, JsonElement original) {
    }
}
