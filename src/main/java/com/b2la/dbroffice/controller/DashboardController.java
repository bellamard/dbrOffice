package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.HelloApplication;
import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.StateOper;
import com.b2la.dbroffice.preference.Storage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.b2la.dbroffice.connexion.Api.*;
import static java.util.concurrent.CompletableFuture.runAsync;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

public class DashboardController implements Initializable {

    @FXML
    private Label username, countAdmin, countOffices, countAgents, countClients, countUsers,
            countSend, countWithdrawal, countDeposit, countFactory, countOpera,countRecov, titleOperation,
            attentCDF, attentUSD, validerUSD, validerCDF, annulersUSD, annulersCDF, usersClients, usersAdmin, usersOfficial, usersAgent, UsersCount;
    private Button btnTaux;
    @FXML
    private AnchorPane home, operation, utilisateur, chargement;
    @FXML
    private TextField searchTaux, fieldUserSearch, getSearchOperation;
    @FXML
    private TableView tableauTaux, tableUtilisateur, tableOperation;
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
    private TableColumn<StreamUser, String> userTPhone, userTSurname, userTlastName;
    @FXML
    private TableColumn<StreamUser, String> userTType, userTAction;
    @FXML
    private TableColumn<Operation, String>tOperRef, tOperType, tOperDev, tOperEtat, tOperAction,tOperDate;
    @FXML
    private TableColumn<Operation, Float> tOperMont;
    @FXML
    private AreaChart<String, Number> diagram;
    @FXML
    private BarChart<String, Number> chartOpera;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargement.setVisible(true);
        getChargement();

    }

    private void getChargement(){


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
                CountUsers cu= solde(clef);
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
                        exit();
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
                    chargement.setVisible(false);

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
                        if(Operat.getState().getLibelle()==StateOper.VALIDER){
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
                    chargement.setVisible(false);
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
                        if(op.getState().getLibelle()==StateOper.VALIDER){
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
                });


                xyC.chartProperty();
                xyU.chartProperty();
                diagram.getData().clear();
                diagram.getData().add(xyC);
                diagram.getData().add(xyU);
                chargement.setVisible(false);
            } catch (Exception e) {
                chargement.setVisible(false);
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
                        .collect(Collectors.groupingBy(Oper-> String.valueOf(Oper.getState().getLibelle())));
                AtomicInteger sommeValiderCDF= new AtomicInteger();
                AtomicInteger sommeValiderUSD= new AtomicInteger();
                AtomicInteger sommeAnnulerCDF= new AtomicInteger();
                AtomicInteger sommeAnnulerUSD= new AtomicInteger();
                AtomicInteger sommeAttenteCDF= new AtomicInteger();
                AtomicInteger sommeAttenteUSD= new AtomicInteger();
                groupeDate.forEach((date,liste)->{

                    for(Operation op: liste){
                        if(op.getState().getLibelle()==StateOper.VALIDER){

                            if(op.getDevice().equals("usd")){
                                sommeValiderUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                sommeValiderCDF.getAndIncrement();
                            }
                        }
                        if(op.getState().getLibelle()==StateOper.ANNULER){

                            if(op.getDevice().equals("usd")){
                                sommeAnnulerUSD.getAndIncrement();
                            }
                            if(op.getDevice().equals("cdf")){
                                sommeAnnulerCDF.getAndIncrement();
                            }
                        }
                        if(op.getState().getLibelle()==StateOper.ATTENTE){

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
                    chargement.setVisible(false);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void viewTaux(){
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
                            try {
                                openDialogueUpCost(c);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                );

                btnSup.setOnAction(
                        e->{
                            Cost c=getTableView().getItems().get(getIndex());
                            System.out.println("Suprimer: "+c.getId());
                            suppressionTaux(c);
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
            chargement.setVisible(false);
            cardLayout("home");;
        });
    }

    private void suppressionTaux(Cost cost){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous Supprimer ?");
        alert.setContentText("Cliquez sur OK pour confirmer la suppression.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("L'utilisateur a confirmé !");
            deleteCost(cost);
            viewTaux();
        } else {
            System.out.println("L'utilisateur a annulé.");
        }
    }


    @FXML
    private void hanchSearch(){
        chargement.setVisible(true);
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
            chargement.setVisible(false);
        });
    }

    @FXML
    protected void onClose() {
        Storage.removeLogin();
        exit();
    }

    // pane Utilisateur

    public void getTableUser(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;

        runAsync(()->{
            List<StreamUser> userList=userPerson(clef);
            assert userList != null;
            userTPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            userTSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
            userTlastName.setCellValueFactory(new PropertyValueFactory<>("lastname"));
            userTType.setCellValueFactory(new PropertyValueFactory<>("type"));

            userTAction.setCellFactory(col->new TableCell<>(){
                private final Button btn= new Button("Aperçus");

                {
                    btn.setOnAction(
                            e->{
                                StreamUser su=getTableView().getItems().get(getIndex());
                                System.out.println("apercus: "+su.getSurname());

                                try {
                                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("apercus-panel.fxml"));
                                    Parent dialogRoot = loader.load();
                                    Stage dialogStage = new Stage();
                                    dialogStage.centerOnScreen();
                                    ApercusController ac= loader.getController();
                                    ac.typeOpera(su);
                                    dialogStage.setResizable(false);
                                    dialogStage.initModality(Modality.APPLICATION_MODAL);
                                    dialogStage.setTitle("Gestion Utilisateur");
                                    dialogStage.setScene(new Scene(dialogRoot));
                                    dialogStage.showAndWait();
                                } catch (IOException ex) {

                                    Alert alert= new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Erreur!!!");
                                    alert.setHeaderText("Avertissement Erreur!!!");
                                    alert.setContentText("Veuillez ressayer: \n"+ex);
                                    alert.showAndWait();
                                    throw new RuntimeException(ex);
                                }


                            }
                    );


                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox boxBtn=new HBox(1, btn);
                        setGraphic(boxBtn);
                    }
                }
            });



            Task<ObservableList<StreamUser>> task= new Task<>() {
                @Override
                protected ObservableList<StreamUser> call() throws Exception {

                    return FXCollections.observableArrayList(userList);
                }
            };
            task.setOnSucceeded(e->tableUtilisateur.setItems(task.getValue()));
            new Thread(task).start();
            runLater(()->{
                int nbreClientsUtil=0;
                int nbreAdminUtil=0;
                int nbreOfficeUtil=0;
                int nbreAgentUtil=0;
                for(StreamUser client: userList){

                    if(client.getType().equals("CLIENT")){
                        nbreClientsUtil++;
                    }

                    if(client.getType().equals("ADMIN")){
                        nbreAdminUtil++;
                    }

                    if(client.getType().equals("OFFICE")){
                        nbreOfficeUtil++;
                    }

                    if(client.getType().equals("AGENT")){
                        nbreAgentUtil++;
                    }

                }

                UsersCount.setText(String.valueOf(userList.size()));
                usersClients.setText(nbreClientsUtil+" Clients");
                usersAgent.setText(nbreAgentUtil+" Agents");
                usersOfficial.setText(nbreOfficeUtil+" Official");
                usersAdmin.setText(nbreAdminUtil+" Admin");

            });



        });

        
        
    }


    @FXML
    private void getTableUserSearch(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;

        runAsync(()->{
            List<StreamUser> userList=userPerson(clef);
            List<StreamUser> searchListUser=userList.stream().filter(tUsers->
                    tUsers.getType().contains(fieldUserSearch.getText())||
                            tUsers.getFirstname().toLowerCase().contains(fieldUserSearch.getText())||
                            tUsers.getFirstname().toUpperCase().contains(fieldUserSearch.getText())||
                            tUsers.getLastname().toLowerCase().contains(fieldUserSearch.getText())||
                            tUsers.getLastname().toUpperCase().contains(fieldUserSearch.getText())||
                            tUsers.getSurname().toLowerCase().contains(fieldUserSearch.getText())||
                            tUsers.getSurname().toUpperCase().contains(fieldUserSearch.getText())||
                            tUsers.getType().toLowerCase().contains(fieldUserSearch.getText())||
                            tUsers.getType().toUpperCase().contains(fieldUserSearch.getText())||
                            tUsers.getPhone().contains(fieldUserSearch.getText())
            ).collect(Collectors.toList());

            assert userList != null;




            Task<ObservableList<StreamUser>> task= new Task<>() {
                @Override
                protected ObservableList<StreamUser> call() throws Exception {

                    return FXCollections.observableArrayList(searchListUser);
                }
            };
            task.setOnSucceeded(e->tableUtilisateur.setItems(task.getValue()));
            new Thread(task).start();
            runLater(()->{
                int nbreClientsUtil=0;
                int nbreAdminUtil=0;
                int nbreOfficeUtil=0;
                int nbreAgentUtil=0;
                for(StreamUser client: searchListUser){

                    if(client.getType().equals("CLIENT")){
                        nbreClientsUtil++;
                    }

                    if(client.getType().equals("ADMIN")){
                        nbreAdminUtil++;
                    }

                    if(client.getType().equals("OFFICE")){
                        nbreOfficeUtil++;
                    }

                    if(client.getType().equals("AGENT")){
                        nbreAgentUtil++;
                    }

                }

                UsersCount.setText(String.valueOf(searchListUser.size()));
                usersClients.setText(nbreClientsUtil+" Clients");
                usersAgent.setText(nbreAgentUtil+" Agents");
                usersOfficial.setText(nbreOfficeUtil+" Official");
                usersAdmin.setText(nbreAdminUtil+" Admin");

            });



        });



    }

    private void gestionBloque(StreamUser user){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Gestion Etat");
        alert.setHeaderText("Voulez-vous Bloquer ?");
        alert.setContentText("Cliquez sur OK pour confirmer les bloquage.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("L'utilisateur a confirmé !");
            getTableUser();
        } else {
            System.out.println("L'utilisateur a annulé.");
        }
    }




    private void cardLayout(String Layout){
        switch (Layout){
            case "home": home.setVisible(true);
            operation.setVisible(false);
            utilisateur.setVisible(false);
            chargement.setVisible(false);
            break;
            case "operation": home.setVisible(false);
                operation.setVisible(true);
                utilisateur.setVisible(false);
                chargement.setVisible(false);
                break;
            case "utilisateur": home.setVisible(false);
                operation.setVisible(false);
                utilisateur.setVisible(true);
                chargement.setVisible(false);
                break;
            default: chargement.setVisible(true);
                home.setVisible(false);
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
        dialogStage.setTitle("Ajouter Taux");
        dialogStage.setScene(new Scene(dialogRoot));
        dialogStage.showAndWait();
    }

    private void openDialogueUpCost(Cost c) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dialogCost.fxml"));
        Parent dialogRoot = loader.load();
        Stage dialogStage = new Stage();
        DialogueCostController dcc= loader.getController();
        dcc.typeOpera(c);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Modification de Taux!!!");
        dialogStage.setScene(new Scene(dialogRoot));
        dialogStage.showAndWait();
    }

    @FXML
    private void openDialogueUser() throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("utilisateur-panel.fxml"));
        Parent dialogRoot = loader.load();
        Stage dialogStage = new Stage();

        dialogStage.centerOnScreen();
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Gestion Utilisateur Taux");
        dialogStage.setScene(new Scene(dialogRoot));
        dialogStage.showAndWait();
    }


    public void viewOperation(){
        LoginResponse clef = Storage.loadLogin();
        assert clef != null;

        tOperRef.setCellValueFactory(new PropertyValueFactory<>("codereference"));
        tOperDev.setCellValueFactory(new PropertyValueFactory<>("device"));
        tOperMont.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tOperDate.setCellValueFactory(new PropertyValueFactory<>("dateoperation"));
        tOperEtat.setCellFactory(col -> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setGraphic(null);
                }else{
                    Operation op = getTableView().getItems().get(getIndex());
                    Label labelIcon=new Label("Etat iconnu", new FontIcon(FontAwesome.QUESTION_CIRCLE));
                    if(op.getState().getLibelle().equals(StateOper.ATTENTE)){
                        labelIcon=new Label("ATTENTE", new FontIcon(FontAwesome.HOURGLASS));
                    }
                    if(op.getState().getLibelle().equals(StateOper.VALIDER)){
                        labelIcon=new Label("VALIDER", new FontIcon(FontAwesome.CHECK_CIRCLE));
                    }
                    if(op.getState().getLibelle().equals(StateOper.ANNULER)){
                        labelIcon=new Label("ANNULER", new FontIcon(FontAwesome.TIMES_CIRCLE));
                    }
                    if(op.getState().getLibelle().equals(StateOper.BLOQUER)){
                        labelIcon=new Label("BLOQUER", new FontIcon(FontAwesome.BAN));
                    }
                    HBox boxLabel = new HBox(2, labelIcon);
                    setGraphic(boxLabel);

                }
            }
        });
        tOperType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {

                    Operation op = getTableView().getItems().get(getIndex());
                    Role exp=op.getExp().getUser().getRole();
                    Role ben=op.getBen().getUser().getRole();
                    Label label = new Label("TYPE INCONNU");
                    if(Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "CLIENT")){
                        label= new Label("ENVOI CLIENT");
                        
                    }
                    if(Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "AGENT")){
                        label=new Label("RETRAIT CLIENT");
                    }
                    if(Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "CLIENT")){
                       label=new Label("DEPOT CLIENT");
                    }
                    if(Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "OFFICE")){
                        label= new Label("RETRAIT AGENT");
                    }
                    if(Objects.equals(exp.getLibelle(), "OFFICE") && Objects.equals(ben.getLibelle(), "AGENT")){
                        label= new Label("DEPOT AGENT");
                    }
                    HBox boxLabel = new HBox(2, label);
                    setGraphic(boxLabel);
                }
            }
        });
        tOperAction.setCellFactory(col -> new TableCell<>() {

            FontIcon iconLock = new FontIcon(FontAwesome.LOCK);
            FontIcon iconApercus = new FontIcon(FontAwesome.FILE_ARCHIVE_O);

            private final Button btnB = new Button("B", iconLock);

            private final Button btnAp = new Button("A", iconApercus);

            {
                btnB.setOnAction(
                        e -> {
                            Operation op = getTableView().getItems().get(getIndex());
                            System.out.println("Bloquer: " + op.getDevice());



                        }
                );

                btnAp.setOnAction(
                        e -> {
                            Operation op = getTableView().getItems().get(getIndex());
                            System.out.println("Apercus: " + op.getId());

                            try {
                                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("apercusOperation.fxml"));
                                Parent dialogRoot = loader.load();
                                Stage dialogStage = new Stage();
                                dialogStage.centerOnScreen();
                                ApercusOperationController aoc= loader.getController();
                                aoc.affichage(op);
                                dialogStage.setResizable(false);
                                dialogStage.initModality(Modality.APPLICATION_MODAL);
                                dialogStage.setTitle("Operation");
                                dialogStage.setScene(new Scene(dialogRoot));
                                dialogStage.showAndWait();
                            }
                            catch (IOException ex) {

                                Alert alert= new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Erreur!!!");
                                alert.setHeaderText("Avertissement Erreur!!!");
                                alert.setContentText("Veuillez ressayer: \n"+ex);
                                alert.showAndWait();
                                throw new RuntimeException(ex);
                            }
                        }
                );
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox boxBtn = new HBox(2);
                    Operation op = getTableView().getItems().get(getIndex());
                    if (op.getState().getLibelle() == StateOper.ATTENTE) {
                        boxBtn.getChildren().add(btnB);
                    }
                    boxBtn.getChildren().add(btnAp);
                    setGraphic(boxBtn);
                }
            }
        });

        runAsync(() -> {
            List<Operation> operationList = listOperation(clef);
            Task<ObservableList<Operation>> task = new Task<>() {
                @Override
                protected ObservableList<Operation> call() throws Exception {

                    return FXCollections.observableArrayList(operationList);
                }
            };
            task.setOnSucceeded(e -> tableOperation.setItems(task.getValue()));
            new Thread(task).start();
            viewDescription(operationList);
            chargement.setVisible(false);

        });

    }

    @FXML
    private void getTableOperationSearch(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;

        runAsync(()->{
            List<Operation> operList=listOperation(clef);
            String searchText = getSearchOperation.getText().toLowerCase();
            List<Operation> searchListOper = operList.stream().filter(tOperation ->
                    tOperation.getDevice().toLowerCase().contains(searchText) ||
                            tOperation.getCodereference().toLowerCase().contains(searchText) ||
                            tOperation.getState().getLibelle().toString().toLowerCase().contains(searchText) ||
                            tOperation.getDateoperation().toLowerCase().contains(searchText) ||

                            tOperation.getBen().getUser().getSurname().toLowerCase().contains(searchText) ||
                            tOperation.getBen().getUser().getFirstname().toLowerCase().contains(searchText) ||
                            tOperation.getBen().getUser().getLastname().toLowerCase().contains(searchText) ||
                            tOperation.getBen().getUser().getRole().getLibelle().toLowerCase().contains(searchText) ||

                            tOperation.getExp().getUser().getSurname().toLowerCase().contains(searchText) ||
                            tOperation.getExp().getUser().getFirstname().toLowerCase().contains(searchText) ||
                            tOperation.getExp().getUser().getLastname().toLowerCase().contains(searchText) ||
                            tOperation.getExp().getUser().getRole().getLibelle().toLowerCase().contains(searchText) ||

                            String.valueOf(tOperation.getAmount()).toLowerCase().contains(searchText) ||
                            String.valueOf(tOperation.getCommission()).toLowerCase().contains(searchText)
            ).collect(Collectors.toList());


            assert operList != null;




            Task<ObservableList<Operation>> task= new Task<>() {
                @Override
                protected ObservableList<Operation> call() throws Exception {

                    return FXCollections.observableArrayList(searchListOper);
                }
            };
            task.setOnSucceeded(e->tableOperation.setItems(task.getValue()));
            new Thread(task).start();


            viewDescription(searchListOper);

        });



    }

    private void viewDescription(List<Operation> persoList){
        runLater(()->{

            int total=persoList.size();
            int clientToClient = 0, clientToAgent = 0, agentToAdmin = 0, enAttente = 0,
                    validees = 0, annulees = 0, bloquees = 0;
            float montantTotalUSD=0,montantTotalCDF=0, montantValideUSD=0, montantValideCDF=0, montantAnnuleUSD=0, montantAnnuleCDF=0, montantAttenteUSD=0, montantAttenteCDF=0;


            for(Operation op: persoList){

                Role exp=op.getExp().getUser().getRole();
                Role ben=op.getBen().getUser().getRole();
                State state= op.getState();
                if(Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "CLIENT")){
                    System.out.println("ENVOI CLIENT ="+clientToClient);
                    clientToClient++;
                }
                if((Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "AGENT"))||
                        (Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "CLIENT"))){
                    System.out.println("RETRAIT CLIENT ="+clientToAgent);
                    clientToAgent++;
                }
                if((Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "OFFICE"))||
                        (Objects.equals(exp.getLibelle(), "OFFICE") && Objects.equals(ben.getLibelle(), "AGENT"))){
                    System.out.println("DEPOT CLIENT ="+agentToAdmin);
                    agentToAdmin++;
                }
                if(Objects.equals(state.getLibelle(), StateOper.ATTENTE)){
                    enAttente++;
                    if(Objects.equals(op.getDevice(), "usd")) {
                        montantAttenteUSD += op.getAmount();
                        System.out.println("Montant En Attente ="+montantAttenteUSD+"  USD");
                    };
                    if(Objects.equals(op.getDevice(), "cdf")) {
                        montantAttenteCDF += op.getAmount();
                        System.out.println("Montant En Annulee ="+montantAttenteCDF+"CDF");
                    };
                    System.out.println("Statut En ATTENTE ="+enAttente);
                }
                if(Objects.equals(state.getLibelle(), StateOper.VALIDER)){
                    validees++;
                    if(Objects.equals(op.getDevice(), "usd")) {
                        montantValideUSD += op.getAmount();
                        System.out.println("Montant En Validee ="+montantValideUSD+"  USD");
                    };
                    if(Objects.equals(op.getDevice(), "cdf")) {
                        montantValideCDF += op.getAmount();
                        System.out.println("Montant En Validee ="+montantValideCDF+"CDF");
                    };
                    System.out.println("Statut En VALIDEE ="+validees);
                }
                if(Objects.equals(state.getLibelle(), StateOper.BLOQUER)){
                    bloquees++;

                    System.out.println("Statut En BLOQUER ="+bloquees);

                }
                if(Objects.equals(state.getLibelle(), StateOper.ANNULER)){
                    annulees++;
                    if(Objects.equals(op.getDevice(), "usd")) {
                        montantAnnuleUSD += op.getAmount();
                        System.out.println("Montant En Annulee ="+montantAnnuleUSD+"  USD");
                    };
                    if(Objects.equals(op.getDevice(), "cdf")) {
                        montantAnnuleCDF += op.getAmount();
                        System.out.println("Montant En Annulee ="+montantAnnuleCDF+"CDF");
                    };
                    System.out.println("Statut En Annulee ="+annulees);

                }
                if(Objects.equals(op.getDevice(), "usd")){
                    montantTotalUSD+=op.getAmount();
                    System.out.println("Montant total= "+montantTotalUSD+" USD");
                }
                if(Objects.equals(op.getDevice(), "cdf")){
                    montantTotalCDF+=op.getAmount();
                    System.out.println("Montant total= "+montantTotalCDF+" CDF");
                }


            }

            titleOperation.setText(String.format("""
                        Nombre total d'opérations : %d
                        - Opérations client → client : %d
                        - le client ↔ agent : %d
                        - Opérations agent ↔ admin : %d
                        
                        Statuts :
                        - En attente : %d
                        - Validées : %d
                        - Annulées : %d
                        - Bloquées : %d
                        
                        Montants :
                        - Total : %.2f USD / %.2f CDF
                        - Validées : %.2f USD / %.2f CDF
                        - En attente : %.2f USD / %.2f CDF
                        - Annulées : %.2f USD / %.2f CDF
                        """, total, clientToClient, clientToAgent, agentToAdmin, enAttente, validees, annulees, bloquees, montantTotalUSD, montantTotalCDF, montantValideUSD, montantValideCDF, montantAttenteUSD, montantAttenteCDF, montantAnnuleUSD, montantAnnuleCDF));



        });
    }

    private void chartOperation(){
        LoginResponse clef=Storage.loadLogin();
        assert clef != null;
        runLater(()->{
            try{
                List<Operation> opeList = operationList(clef);
                assert opeList != null;

                XYChart.Series<String, Number> xyC2CUSD = new XYChart.Series<>();
                xyC2CUSD.setName("C2C USD");
                XYChart.Series<String, Number> xyC2CCDF = new XYChart.Series<>();
                xyC2CCDF.setName("C2C CDF");
                XYChart.Series<String, Number> xyC2AUSD = new XYChart.Series<>();
                xyC2AUSD.setName("C2A USD");
                XYChart.Series<String, Number> xyC2ACDF = new XYChart.Series<>();
                xyC2ACDF.setName("C2A CDF");
                XYChart.Series<String, Number> xyA2OUSD = new XYChart.Series<>();
                xyA2OUSD.setName("A2O USD");
                XYChart.Series<String, Number> xyA2OCDF = new XYChart.Series<>();
                xyA2OCDF.setName("A2O CDF");


                Map<String, List<Operation>> groupeDateo = opeList.stream()
                        .collect(Collectors.groupingBy(op -> {
                            OffsetDateTime dateTime = OffsetDateTime.parse(op.getDateoperation());
                            return dateTime.getYear() + "-" + String.format("%02d", dateTime.getMonthValue());
                        }));


                groupeDateo.forEach((date, liste) -> {
                    float C2CUSD = 0, C2CCDF = 0, C2AUSD = 0, C2ACDF = 0, A2OUSD = 0, A2OCDF = 0;

                    for (Operation op1 : liste) {
                        if (op1.getState().getLibelle() == StateOper.VALIDER) {
                            Role exp = op1.getExp().getUser().getRole();
                            Role ben = op1.getBen().getUser().getRole();
                            float amount = op1.getAmount();
                            if (op1.getDevice().equalsIgnoreCase("usd")) {
                                if (Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "CLIENT"))
                                    C2CUSD += amount;
                                else if ((Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "AGENT")) ||
                                        (Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "CLIENT")))
                                    C2AUSD += amount;
                                else if ((Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "OFFICE")) ||
                                        (Objects.equals(exp.getLibelle(), "OFFICE") && Objects.equals(ben.getLibelle(), "AGENT")))
                                    A2OUSD += amount;
                            }
                            else if (op1.getDevice().equalsIgnoreCase("cdf")) {
                                if (Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "CLIENT"))
                                    C2CCDF += amount;
                                else if ((Objects.equals(exp.getLibelle(), "CLIENT") && Objects.equals(ben.getLibelle(), "AGENT")) ||
                                        (Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "CLIENT")))
                                    C2ACDF += amount;
                                else if ((Objects.equals(exp.getLibelle(), "AGENT") && Objects.equals(ben.getLibelle(), "OFFICE")) ||
                                        (Objects.equals(exp.getLibelle(), "OFFICE") && Objects.equals(ben.getLibelle(), "AGENT")))
                                    A2OCDF += amount;
                            }
                        }
                    }

                    // Ajout des valeurs au chart
                    xyC2CUSD.getData().add(new XYChart.Data<>(date, (C2CUSD*2850)));
                    xyC2CCDF.getData().add(new XYChart.Data<>(date, C2CCDF));
                    xyC2AUSD.getData().add(new XYChart.Data<>(date, (C2AUSD*2850)));
                    xyC2ACDF.getData().add(new XYChart.Data<>(date, C2ACDF));
                    xyA2OUSD.getData().add(new XYChart.Data<>(date, (A2OUSD*2850)));
                    xyA2OCDF.getData().add(new XYChart.Data<>(date, A2OCDF));
                });

                chartOpera.getData().clear();
                chartOpera.getData().addAll(
                        xyC2CCDF, xyC2CUSD,
                        xyC2ACDF, xyC2AUSD, xyA2OUSD, xyA2OCDF
                );

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    protected void goToHome(){
        String card="home";
        cardLayout(card);
        getChargement();

    }

    @FXML
    protected void goToOperation(){
        String card="operation";
        cardLayout(card);
        viewOperation();
        chartOperation();

    }
    @FXML
    protected void goToUtilisateur(){
        String card="utilisateur";
        cardLayout(card);
        getTableUser();
    }
    @FXML
    protected void goToCommission(){
        String card="commission";
        cardLayout(card);
        getTableUser();
    }


}
