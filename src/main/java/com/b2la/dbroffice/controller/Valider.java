package com.b2la.dbroffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

public class Valider {

    @FXML
    private VBox mainContainer;

    @FXML
    private HBox headerBox;

    @FXML
    private FontIcon unlockIcon;

    @FXML
    private Label headerLabel;

    @FXML
    private VBox inputContainer;

    @FXML
    private Label keyLabel;

    @FXML
    private TextField activationKeyField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button validateButton;

    @FXML
    private Separator headerSeparator;

    @FXML
    public void initialize() {
        // Initialisation après le chargement du FXML
        errorLabel.setVisible(false);

        // Événement sur le bouton
        validateButton.setOnAction(event -> onValidateClicked());
    }

    private void onValidateClicked() {
        String key = activationKeyField.getText().trim();

        if (key.isEmpty()) {
            showError("Veuillez entrer une clé d’activation.");
            return;
        }

        // Exemple de validation fictive
        if (key.length() < 6) {
            showError("La clé doit contenir au moins 6 caractères.");
            return;
        }

        // Supposons que la clé est correcte
        clearError();
        System.out.println("Clé acceptée : " + key);

        // TODO : Insérer ici l’appel à ton API, ta logique de validation, etc.
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
