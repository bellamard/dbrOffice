package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.DashboardApplication;
import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.connexion.Api;
import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class HelloController {
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
    protected void onConnexion(){

        paneLoading.setVisible(true);
        btnConnexion.setVisible(false);
        errorTitle.setVisible(false);
        if(fieldUsername.getText()==""||fieldUsername.getText()==null){
            errorTitle.setText("le Champ Utilisateur ne peut pas etre vide !!!");
            errorTitle.setVisible(true);
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            return;
        }

        if(fieldPassword.getText()==""||fieldUsername.getText()==null){
            errorTitle.setText("le Champ Mot de passe ne peut pas etre vide");
            errorTitle.setVisible(true);
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            return;
        }



        LoginRequest request= new LoginRequest(fieldUsername.getText(), fieldPassword.getText());
        LoginResponse response=   Api.login(request);
        if(response!= null){
            Storage.saveLogin(response);

        }else{
            fieldPassword.setText("");
            fieldUsername.setText("");
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            errorTitle.setText("Erreur de connexion !!!");
            errorTitle.setVisible(true);
        }
    };

    @FXML
    protected void handleKeyPressed(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {
            onConnexion();
        }
    }

    @FXML
    protected void onClose() {
        Storage.removeLogin();
        System.exit(0);
    }
}