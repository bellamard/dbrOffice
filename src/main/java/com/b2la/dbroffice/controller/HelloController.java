package com.b2la.dbroffice.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class HelloController {
    @FXML
    private Label welcomeText, errorTitle;
    @FXML
    private Button btnConnexion;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Pane paneLoading;



    @FXML
    protected void onConnexion(){

    };

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}