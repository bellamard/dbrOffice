package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.connexion.Api;
import com.b2la.dbroffice.dao.LoginRequest;
import com.b2la.dbroffice.dao.LoginResponse;
import com.b2la.dbroffice.preference.Storage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

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


        if(fieldUsername.getText().isEmpty() || fieldUsername.getText().equals("+243") ||fieldUsername.getText()==null){
            errorTitle.setText("le Champ Utilisateur ne peut pas etre vide !!!");
            errorTitle.setVisible(true);
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            try {
                Thread.sleep(3*1000);
                errorTitle.setText("");
                errorTitle.setVisible(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if(fieldPassword.getText().isEmpty()||fieldUsername.getText()==null){
            errorTitle.setText("le Champ Mot de passe ne peut pas etre vide");
            errorTitle.setVisible(true);
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            try {
                Thread.sleep(3*1000);
                errorTitle.setText("");
                errorTitle.setVisible(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        LoginRequest request= new LoginRequest(fieldUsername.getText(), fieldPassword.getText());
        LoginResponse response=   Api.login(request);
        if(response!= null){
            Storage.saveLogin(response);
            onEnterDashboard();
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
        }
        else{
            fieldPassword.setText("");
            fieldUsername.setText("+243");
            paneLoading.setVisible(false);
            btnConnexion.setVisible(true);
            errorTitle.setText("Erreur de connexion !!!");
            errorTitle.setVisible(true);
            try {
                Thread.sleep(3*1000);
                errorTitle.setText("");
                errorTitle.setVisible(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    private void onEnterDashboard(){
        try {
            FXMLLoader loader= new FXMLLoader(HelloApplication.class.getResource("dashboard-view-v.fxml"));
            Parent root = loader.load();
            Scene scene= new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
            Stage stage= (Stage)fieldUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("DASHBOARD");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}