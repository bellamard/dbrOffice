package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Cost;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static com.b2la.dbroffice.connexion.Api.addCost;
import static com.b2la.dbroffice.connexion.Api.updateCost;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.runLater;

public class DialogueCostController implements Initializable {

    @FXML
    private TextField fieldPercent, fieldMin,fieldMax;
    @FXML
    Label textValidateur;
    @FXML
    ComboBox<String> devise;
    @FXML
    Button btnValider;

    boolean typeOperation=false;
    int id;


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

        devise.getItems().addAll("usd", "cdf");
        devise.setValue("usd");
    }

    @FXML
    private void onValiderOperation(){
        if(typeOperation==true){
            validerModifier();
            resertOpera();
            return;
        }
        validerDialogue();
        resertOpera();
    }



    public void validerDialogue(){
        textValidateur.setVisible(false);
        runAsync(()->{

            float percent= Float.parseFloat(fieldPercent.getText());
            float max= Float.parseFloat(fieldMax.getText());
            float min= Float.parseFloat(fieldMin.getText());
            String _devise= devise.getValue();
            addCost(new Cost(_devise, max, min, percent));
            runLater(()->{
                textValidateur.setVisible(true);
            });

        });
        System.out.println("Ajouter Taux Valider");

    }

    public void validerModifier(){
        textValidateur.setVisible(false);
        runAsync(()->{
            float percent= Float.parseFloat(fieldPercent.getText());
            float max= Float.parseFloat(fieldMax.getText());
            float min= Float.parseFloat(fieldMin.getText());
            String _devise= devise.getValue();
            updateCost(new Cost(id, _devise, max, min, percent));
            runLater(()->{
                textValidateur.setVisible(true);
            });


        });
        System.out.println("Modification Taux Valider");

    }

    private void closeDialog(){


        System.
    }

    public void typeOpera(Cost cost){
            id=cost.getId();
            fieldPercent.setText(String.valueOf(cost.getPercent()));
            fieldMax.setText(String.valueOf(cost.getMax()));
            fieldMin.setText(String.valueOf(cost.getMin()));
            devise.setValue(String.valueOf(cost.getDevices()));
            typeOperation=true;

    }

    private void resertOpera(){
        id=0;
        fieldPercent.setText("");
        fieldMax.setText("");
        fieldMin.setText("");
        devise.setValue("");
        typeOperation=false;
        closeDialog();
    }
}
