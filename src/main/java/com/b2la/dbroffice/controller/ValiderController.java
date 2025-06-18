package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import static com.b2la.dbroffice.connexion.Api.activation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValiderController {

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

    int counter=3;

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

        Map<String, Integer> token=new HashMap<>();
        token.put("code", Integer.valueOf(key));

        if(!activation(token)){
            showError("La clé est invalide");
            if(counter>0){
                counter--;
                return;
            }
            Stage stage=(Stage) validateButton.getScene().getWindow();
            stage.close();
        }else{

            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dashboard-view-v.fxml"));
                Parent root = loader.load();
                DashboardController dbc=(DashboardController) loader.getController();
                dbc.getTableUser();
                Stage stage=(Stage) validateButton.getScene().getWindow();
                stage.close();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

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
