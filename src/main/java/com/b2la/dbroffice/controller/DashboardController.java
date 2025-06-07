package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.connexion.Api;
import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.Storage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.b2la.dbroffice.connexion.Api.*;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

public class DashboardController implements Initializable {

    @FXML
    private Label username, countAdmin, countOffices, countAgents, countClients, countUsers,
            countSend, countWithdrawal, countDeposit, countFactory, countOpera,countRecov,
            attentCDF, attentUSD, validerUSD, validerCDF, annulersUSD, annulersCDF;
    private Button btnTaux;
    @FXML
    private AnchorPane home, operation, utilisateur;
    @FXML
    private TextField searchTaux;
    @FXML
    private TableView tableauTaux;
    @FXML
    private TableColumn<Cost, String> TCdevices;
    @FXML
    private TableColumn<Cost, Float> TCmin;
    @FXML
    private TableColumn<Cost, Float> TCmax;
    @FXML
    private TableColumn<Cost, Float> TCpourcent;
    @FXML
    private TableColumn<Cost, String> TCactions;
    @FXML
    private AreaChart<String, Number> diagram;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profil();
        userCount();
        Operation();
        chart();
        sommeOperation();
        viewTaux();
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
                        if(Operat.getState().getLibelle().equals("VALIDER")){
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

                XYChart.Series<String, Number> xyC= new XYChart.Series<>();
                xyC.setName("CDF");
                XYChart.Series<String, Number> xyU= new XYChart.Series<>();
                xyU.setName("USD");

                Map<String, List<Operation>> groupeDate=operaList.stream()
                        .collect(Collectors.groupingBy(Oper->LocalDate.parse(Oper.getDateoperation(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString()
                        ));

                groupeDate.forEach((date,liste)->{
                    AtomicInteger nombreUSD= new AtomicInteger();
                    AtomicInteger nombreCDF= new AtomicInteger();
                    liste.forEach(op->{
                        if(op.getState().getLibelle().equals("VALIDER")){
                            if(op.getDevice().equals("usd")){
                                nombreUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                nombreCDF.getAndIncrement();
                            }
                        }
                    });

                    xyC.getData().add(new XYChart.Data<>(date, nombreCDF));
                    xyU.getData().add(new XYChart.Data<>(date, nombreUSD));
                    xyU.chartProperty();
                });

                diagram.getData().add(xyC);
                diagram.getData().add(xyU);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void sommeOperation(){

        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runAsync(()->{
            try{
                List<Operation> operaList=operationList(clef);
                assert operaList != null;

                Map<String, List<Operation>> groupeDate=operaList.stream()
                        .collect(Collectors.groupingBy(Oper->Oper.getState().getLibelle()));
                AtomicInteger sommeValiderCDF= new AtomicInteger();
                AtomicInteger sommeValiderUSD= new AtomicInteger();
                AtomicInteger sommeAnnulerCDF= new AtomicInteger();
                AtomicInteger sommeAnnulerUSD= new AtomicInteger();
                AtomicInteger sommeAttenteCDF= new AtomicInteger();
                AtomicInteger sommeAttenteUSD= new AtomicInteger();
                groupeDate.forEach((date,liste)->{

                    for(Operation op: liste){
                        if(op.getState().getLibelle().equals("VALIDER")){

                            if(op.getDevice().equals("usd")){
                                sommeValiderUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                sommeValiderCDF.getAndIncrement();
                            }
                        }
                        if(op.getState().getLibelle().equals("ANNULER")){

                            if(op.getDevice().equals("usd")){
                                sommeAnnulerUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                sommeAnnulerCDF.getAndIncrement();
                            }
                        }
                        if(op.getState().getLibelle().equals("ATTENTE")){

                            if(op.getDevice().equals("usd")){
                                sommeAttenteUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                sommeAttenteCDF.getAndIncrement();
                            }
                        }
                    }
                });
                runLater(()->{
                    attentCDF.setText(String.valueOf(sommeAttenteCDF));
                    attentUSD.setText(String.valueOf(sommeAttenteUSD));
                    validerUSD.setText(String.valueOf(sommeValiderUSD));
                    validerCDF.setText(String.valueOf(sommeValiderCDF));
                    annulersUSD.setText(String.valueOf(sommeAnnulerUSD));
                    annulersCDF.setText(String.valueOf(sommeAnnulerCDF));
                });


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void viewTaux(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;

        TCpourcent.setCellValueFactory(new PropertyValueFactory<>("percent"));
        TCdevices.setCellValueFactory(new PropertyValueFactory<>("devices"));
        TCmax.setCellValueFactory(new PropertyValueFactory<>("max"));
        TCmin.setCellValueFactory(new PropertyValueFactory<>("min"));

        TCactions.setCellFactory(col->new TableCell<>(){
            private final Button btn= new Button("M");
            private final Button btnSup= new Button("S");
            {
                btn.setOnAction(
                        e->{
                            Cost c=getTableView().getItems().get(getIndex());
                            System.out.println("modifier: "+c.getDevices());
                        }
                );

                btnSup.setOnAction(
                        e->{
                            Cost c=getTableView().getItems().get(getIndex());
                            System.out.println("Suprimer: "+c.getId());
                        }
                );
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox boxBtn=new HBox(2, btn, btnSup);
                    setGraphic(boxBtn);
                }
            }
        });



        runAsync(()->{

            Task<ObservableList<Cost>> task= new Task<>() {
                @Override
                protected ObservableList<Cost> call() throws Exception {
                    List<Cost> costL = costList(clef);
                    return FXCollections.observableArrayList(costL);
                }
            };
            task.setOnSucceeded(e->tableauTaux.setItems(task.getValue()));
            new Thread(task).start();
        });
    }

    @FXML
    private void hanchSearch(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runAsync(()->{

            Task<ObservableList<Cost>> task= new Task<>() {
                @Override
                protected ObservableList<Cost> call() throws Exception {
                    List<Cost> costL = costList(clef);
                    assert costL != null;
                    List<Cost> searchList=costL.stream().filter(tcCost->
                       tcCost.getDevices().contains(searchTaux.getText())||
                               String.valueOf(tcCost.getMin()).contains(searchTaux.getText())||
                               String.valueOf(tcCost.getMax()).contains(searchTaux.getText())||
                               String.valueOf(tcCost.getPercent()).contains(searchTaux.getText())
                    ).collect(Collectors.toList());
                    if(!searchTaux.getText().isEmpty()){
                        System.out.println("nombre des elements :"+searchList.size());
                        return FXCollections.observableArrayList(searchList);
                    }
                        System.out.println(searchTaux.getText());
                        return FXCollections.observableArrayList(costL);


                }
            };
            task.setOnSucceeded(e->tableauTaux.setItems(task.getValue()));
            new Thread(task).start();
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
    private void openDialogueCost() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dialogCost.fxml"));
        Parent dialogRoot = loader.load();
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Boîte de dialogue personnalisée");
        dialogStage.setScene(new Scene(dialogRoot));
        dialogStage.showAndWait();
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
