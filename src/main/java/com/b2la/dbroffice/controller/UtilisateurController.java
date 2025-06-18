package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.dao.Role;
import com.b2la.dbroffice.dao.User;
import com.b2la.dbroffice.preference.RoleType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static com.b2la.dbroffice.connexion.Api.addUser;

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
        typeCompteCombo.getItems().addAll(RoleType.AGENT.name(),RoleType.OFFICE.name());
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


                try {

                    if(getaddUser()==null){
                        openDialogueActivation();
                        Stage stage = (Stage) nextButton.getScene().getWindow();
                        stage.close();
                    }else {
                        Alert alert= new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur!!!");
                        alert.setHeaderText("Avertissement Erreur!!!");
                        alert.setContentText("Veuillez ressayer: \nL'action n'a pas abouti ");
                        alert.showAndWait();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


        }
    }

    private User getaddUser(){
        String firstname= nomField.getText();
        String lastName= postnomField.getText();
        String surname= prenomField.getText();
        String telephone= telephoneField.getText();
        String email= emailField.getText();
        String motdepasse=passwordField.getText();
        String dateAnniv= String.valueOf(dateNaissanceField.getValue());
        String type=typeCompteCombo.getValue();

        Role role= new Role();
        User user=new User();
        user.setFirstname(firstname);
        user.setLastname(lastName);
        user.setSurname(surname);
        user.setPhone(telephone);
        user.setEmail(email);
        user.setPassword(motdepasse);
        user.setDatebirth(dateAnniv);
        if(type==RoleType.OFFICE.name()){
            role.setLibelle(RoleType.OFFICE.name());
            user.setRole(role);
            return addUser(user, "https://dbr.b2la.online/Users/provider");
        }
        if(type==RoleType.AGENT.name()){
            role.setLibelle(RoleType.AGENT.name());
            user.setRole(role);
            return addUser(user, "https://dbr.b2la.online/Users/agent");
        }

        role.setLibelle(RoleType.CLIENT.name());
        user.setRole(role);
        return addUser(new User(), "https://dbr.b2la.online/Users");

    }

    private void openDialogueActivation() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("valider.fxml"));
        Parent dialogRoot = loader.load();
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Activation Compte");
        dialogStage.setScene(new Scene(dialogRoot));
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
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
