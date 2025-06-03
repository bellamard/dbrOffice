package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.connexion.Api;
import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.Storage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.b2la.dbroffice.connexion.Api.operationList;
import static com.b2la.dbroffice.connexion.Api.userPerson;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

public class DashboardController implements Initializable {

    @FXML
    private Label username, countAdmin, countOffices, countAgents, countClients, countUsers,
            countSend, countWithdrawal, countDeposit, countFactory, countOpera,countRecov,
            amountSendUSD, amountWithdrawalUSD, amountDepositUSD,
            amountSendCDF, amountWithdrawalCDF, amountDepositCDF;
    private Button btnTaux;
    @FXML
    private AnchorPane home, operation, utilisateur;
    @FXML
    private TextField searchTaux;
    @FXML
    private TableView tableTaux;
    @FXML
    private AreaChart<LocalDate, Number> diagram;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profil();
        userCount();
        Operation();
        chart();
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
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runAsync(()->{
            try {
                List<Operation> operaList=operationList(clef);
                assert operaList != null;
                runLater(()->{
                    countOpera.setText(String.valueOf(operaList.size()));
                    int nbreSend=0;
                    int nbreDrawl=0;
                    int nbreDepot=0;
                    int nbreDepotAg=0;
                    int nbreDrawlAg=0;
                    Role roleBen;
                    Role roleExp;
                    for(Operation Operat: operaList){
                        roleBen=Operat.getBen().getUser().getRole();
                        roleExp= Operat.getExp().getUser().getRole();

                        if(roleBen.getLibelle().equals("CLIENT") && roleExp.getLibelle().equals("CLIENT")){
                            nbreSend++;
                        }

                        if(roleBen.getLibelle().equals("AGENT") && roleExp.getLibelle().equals("CLIENT")){
                            nbreDrawl++;
                        }

                        if(roleBen.getLibelle().equals("CLIENT") && roleExp.getLibelle().equals("AGENT")){
                            nbreDepot++;
                        }

                        if(roleBen.getLibelle().equals("AGENT") && roleExp.getLibelle().equals("OFFICE")){
                            nbreDepotAg++;
                        }
                        if(roleBen.getLibelle().equals("OFFICE") && roleExp.getLibelle().equals("AGENT")){
                            nbreDrawlAg++;
                        }
                    }
                    countSend.setText(String.valueOf(nbreSend));
                    countWithdrawal.setText(String.valueOf(nbreDrawl));
                    countDeposit.setText(String.valueOf(nbreDepot));
                    countFactory.setText(String.valueOf(nbreDepotAg));
                    countRecov.setText(String.valueOf(nbreDrawlAg));
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

    private void chart(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;

        runLater(()->{
            try{
                List<Operation> operaList=operationList(clef);
                assert operaList != null;

                XYChart.Series<LocalDate, Number> xyC= new XYChart.Series<>();

                Map<LocalDate, List<Operation>> groupeDate=operaList.stream()
                        .collect(Collectors.groupingBy(Oper->Oper.getDateoperation().toString()::toLoc));

                groupeDate.forEach((date,liste)->{
                    AtomicInteger nombre= new AtomicInteger();
                    liste.forEach(op->{
                        nombre.getAndIncrement();
                    });
                    xyC.getData().add(new XYChart.Data<>(date, nombre));
                });
                diagram.getData().add(xyC);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

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
