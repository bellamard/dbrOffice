package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Cost;
import com.b2la.dbroffice.dao.StreamUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ApercusController {

    @FXML private ComboBox comboType;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPostnom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldPhone;
    @FXML private TextField fieldEmail;
    @FXML private Button btnModifier, btnCommission;
    int id;
    boolean typeOperation=false;
    boolean isModifiable=false;
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList(
                "CLIENT", "AGENT", "OFFICIEL", "ADMIN"
        ));
        comboType.setDisable(true);
    }


    public void typeOpera(StreamUser su){
        id=su.getId();
        fieldEmail.setText(String.valueOf(su.getEmail()));
        fieldPhone.setText(String.valueOf(su.getPhone()));
        fieldNom.setText(String.valueOf(su.getLastname()));
        fieldPostnom.setText(String.valueOf(su.getFirstname()));
        fieldPrenom.setText(String.valueOf(su.getSurname()));
        comboType.setValue(String.valueOf(su.getType()));
        if(su.getType().equals("CLIENT"))btnCommission.setDisable(true);
        typeOperation=true;

    }


    private void getActiveModifier(){
        isModifiable=true;
        fieldEmail.setEditable(true);
        fieldPhone.setEditable(true);
        fieldNom.setEditable(true);
        fieldPostnom.setEditable(true);
        fieldPrenom.setEditable(true);
        comboType.setDisable(false);
        btnModifier.setText("Valider");
    }

    private void setModifier(){
        System.out.println("modifier le "+fieldNom.getText());
        Stage stage = (Stage) btnModifier.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void getModifierAction(){
        if(isModifiable){
            setModifier();
        }
        getActiveModifier();
    }
}
