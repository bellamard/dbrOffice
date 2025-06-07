package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Cost;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static com.b2la.dbroffice.connexion.Api.addCost;
import static java.util.concurrent.CompletableFuture.runAsync;

public class DialogueCostController implements Initializable {

    @FXML
    private TextField fieldPercent, fieldMin,fieldMax;
    @FXML
    ComboBox<String> devise;
    @FXML
    Button btnValider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UnaryOperator<TextFormatter.Change> numericFilterPercent = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };

        UnaryOperator<TextFormatter.Change> numericFilterMin = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };

        UnaryOperator<TextFormatter.Change> numericFilterMax = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };

        // Applique le filtre au TextField
        TextFormatter<String> textFormatter = new TextFormatter<>(numericFilterPercent);
        fieldPercent.setTextFormatter(textFormatter);

        TextFormatter<String> textFormatterMin = new TextFormatter<>(numericFilterMin);
        fieldMin.setTextFormatter(textFormatterMin);

        TextFormatter<String> textFormatterMax = new TextFormatter<>(numericFilterMax);
        fieldMax.setTextFormatter(textFormatterMax);

        devise.getItems().addAll("USD", "CDF");
        devise.setValue("USD");
    }


    public void cancelDialogue(){
        Stage stage = (Stage) fieldMax.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void validerDialogue(){
        runAsync(()->{

            float percent= Float.parseFloat(fieldPercent.getText());
            float max= Float.parseFloat(fieldPercent.getText());
            float min= Float.parseFloat(fieldPercent.getText());
            String _devise= devise.getValue();

            cancelDialogue();
            addCost(new Cost(_devise, max, min, percent));
        });
    }
}
