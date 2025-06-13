package com.b2la.dbroffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class UtilisateurController {
    @FXML private VBox step1, step2, step3;
    @FXML private Button nextButton, backButton;

    @FXML private TextField nomField, postnomField, prenomField;
    @FXML private TextField telephoneField, emailField;
    @FXML private DatePicker dateNaissanceField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> typeCompteCombo;
    @FXML private Label message;

    private int currentStep = 1;

    @FXML
    public void initialize() {
        typeCompteCombo.getItems().addAll("CLIENT","AGENT","OFFICE");
        showStep(currentStep);
    }

    private void showStep(int step) {
        step1.setVisible(step == 1);
        step1.setManaged(step == 1);

        step2.setVisible(step == 2);
        step2.setManaged(step == 2);

        step3.setVisible(step == 3);
        step3.setManaged(step == 3);

        backButton.setDisable(step == 1);
        nextButton.setText(step == 3 ? "Créer un compte" : "Suivant");

    }

    @FXML
    private void handleNext() {
        if (!validateCurrentStep())
        {
            message.setText("Formulaire Incorrect!");
            return;
        }

        if (currentStep < 3) {
            message.setText("");
            currentStep++;
            showStep(currentStep);
        } else {
            System.out.println("✅ Formulaire soumis !");
            // TODO : Traitement du formulaire final ici
        }
    }

    @FXML
    private void handleBack() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
        }
    }

    private boolean validateCurrentStep() {
        boolean valid = true;

        switch (currentStep) {
            case 1:
                valid &= validateField(nomField);
                valid &= validateField(postnomField);
                valid &= validateField(prenomField);
                break;

            case 2:
                valid &= validateField(telephoneField);
                valid &= validateDate(dateNaissanceField);
                valid &= validateEmail(emailField);
                break;

            case 3:
                valid &= validateField(passwordField);
                valid &= validateComboBox(typeCompteCombo);
                break;
        }

        return valid;
    }

    private boolean validateField(TextField field) {
        boolean isValid = field.getText() != null && !field.getText().trim().isEmpty();
        field.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    private boolean validateDate(DatePicker picker) {
        boolean isValid = picker.getValue() != null;
        picker.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    private boolean validateEmail(TextField field) {
        String email = field.getText();
        boolean isValid = email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        field.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }

    private boolean validateComboBox(ComboBox<?> comboBox) {
        boolean isValid = comboBox.getValue() != null;
        comboBox.setStyle(isValid ? "" : "-fx-border-color: red;");
        return isValid;
    }
}
