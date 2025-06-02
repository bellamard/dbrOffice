package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.connexion.Api;
import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.Storage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.b2la.dbroffice.connexion.Api.userPerson;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

public class DashboardController implements Initializable {

    @FXML
    private Label username, countAdmin, countOffices, countAgents, countClients, countUsers,
            countSend, countWithdrawal, countDeposit, countFactory,
            amountSendUSD, amountWithdrawalUSD, amountDepositUSD,
            amountSendCDF, amountWithdrawalCDF, amountDepositCDF;
    private Button btnTaux;
    @FXML
    private AnchorPane home, operation, utilisateur;
    private TextField searchTaux;
    private TableView tableTaux;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profil();
        userCount();
    }

    private CountUsers profil(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runAsync(()->{
            try{
                CountUsers cu=Api.solde(clef);
                assert cu != null;
                User usePerson=cu.getUser();
                Role role= usePerson.getRole();
                runLater(()->{
                    username.setText(usePerson.getSurname()+" "+usePerson.getFirstname());

                    if (!role.getLibelle().equals("OFFICE")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur!!!");
                        alert.setHeaderText("Avertissement Erreur!!!");
                        alert.setContentText("tu n'as pas access avec ce role " + role.getLibelle());
                        alert.showAndWait();
                        // Attention : mieux que System.exit(0) ici, il faut fermer proprement la fenêtre.
                        Platform.exit();
                    }
                });


            } catch (Exception e) {
                runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur de connexion");
                    errorAlert.setHeaderText("Impossible de contacter le serveur.");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                    exit();
                });
                throw new RuntimeException(e);
            }

        });
        return null;
    }

    private void userCount(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runAsync(()->{
            try {
                List<StreamUser> userList=userPerson(clef);
                assert userList != null;
                runLater(()->{
                    countUsers.setText(String.valueOf(userList.size()));
                    int nbreClients=0;
                    int nbreAdmin=0;
                    int nbreOffice=0;
                    int nbreAgent=0;
                    for(StreamUser client: userList){

                        if(client.getType().equals("CLIENT")){
                            nbreClients++;
                        }

                        if(client.getType().equals("ADMIN")){
                            nbreAdmin++;
                        }

                        if(client.getType().equals("OFFICE")){
                            nbreOffice++;
                        }

                        if(client.getType().equals("AGENT")){
                            nbreAgent++;
                        }
                    }
                    countAdmin.setText(String.valueOf(nbreAdmin));
                    countAgents.setText(String.valueOf(nbreAgent));
                    countOffices.setText(String.valueOf(nbreOffice));
                    countClients.setText(String.valueOf(nbreClients));

                });


            } catch (Exception e) {
                runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur de connexion");
                    errorAlert.setHeaderText("Impossible de contacter le serveur.");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                    exit();
                });
                throw new RuntimeException(e);
            }
        });
    }

    private void Operation(){

    }


    @FXML
    protected void onClose() {
        Storage.removeLogin();
        exit();
    }



    private void cardLayout(String Layout){
        switch (Layout){
            case "home": home.setVisible(true);
            operation.setVisible(false);
            utilisateur.setVisible(false);
            break;
            case "operation": home.setVisible(false);
                operation.setVisible(true);
                utilisateur.setVisible(false);
                break;
            case "utilisateur": home.setVisible(false);
                operation.setVisible(false);
                utilisateur.setVisible(true);
                break;
            default: home.setVisible(true);
                operation.setVisible(false);
                utilisateur.setVisible(false);
        }
    }


    @FXML
    protected void goToHome(){
        String card="home";
        cardLayout(card);

    }

    @FXML
    protected void goToOperation(){
        String card="operation";
        cardLayout(card);

    }
    @FXML
    protected void goToUtilisateur(){
        String card="utilisateur";
        cardLayout(card);

    }
}
