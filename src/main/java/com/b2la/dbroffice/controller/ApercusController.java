package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.*;
import com.b2la.dbroffice.preference.RoleType;
import com.b2la.dbroffice.preference.Storage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.b2la.dbroffice.connexion.Api.*;

public class  ApercusController {

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
        User us= new User();
        us.setId(id);
        us.setFirstname(fieldNom.getText());
        us.setLastname(fieldPostnom.getText());
        us.setSurname(fieldPrenom.getText());
        us.setPhone(fieldPhone.getText());
        us.setEmail(fieldEmail.getText());
        Role role= new Role();
        if(comboType.getValue()==RoleType.AGENT.name())role.setLibelle(RoleType.AGENT.name());
        if(comboType.getValue()==RoleType.ADMIN.name())role.setLibelle(RoleType.ADMIN.name());
        if(comboType.getValue()==RoleType.CLIENT.name())role.setLibelle(RoleType.CLIENT.name());
        if(comboType.getValue()==RoleType.OFFICE.name())role.setLibelle(RoleType.OFFICE.name());
        us.setRole(role);
        updateUser(us);
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

    @FXML
    private void getSoldeUser(){
        LoginResponse clef= Storage.loadLogin();
        assert clef != null;

        try{
            List<Accounts> accountsList = listAccounts(clef);
            assert accountsList != null;
            Optional<Accounts> accounts=accountsList.stream()
                    .filter(acc->acc.getUser().getId()==id)
                    .findFirst();
            Alert alert= new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SOLDE");
            alert.setHeaderText(accounts.get().getUser().getSurname()+" "
                    +accounts.get().getUser().getFirstname());
            alert.setContentText(String.format("""
                             🔸Votre Solde est :🔸
                                •  %, .2f CDF
                                • $%, .2f USD
                            """
                    , accounts.get().getCdf()
                    , accounts.get().getUsd()));
            alert.showAndWait();

        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur!!!");
            alert.setHeaderText("Avertissement Erreur!!!");
            alert.setContentText("votre compte de n'est pas activee");
            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void getCommisonUser(){
        LoginResponse clef= Storage.loadLogin();
        assert clef != null;

        try{
            List<Commission> commissionList = listCommissions(clef);
            assert commissionList != null;
            Optional<Commission> accounts=commissionList.stream()
                    .filter(acc->acc.getUser().getId()==id)
                    .findFirst();
            Alert alert= new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SOLDE");
            alert.setHeaderText(accounts.get().getUser().getSurname()+" "
                    +accounts.get().getUser().getFirstname());
            alert.setContentText(String.format("""
                             🔸Votre Solde est :🔸
                                •  %, .2f CDF
                                • $%, .2f USD
                            """
                    , accounts.get().getCdfwin()
                    , accounts.get().getUsdwin()));
            alert.showAndWait();

        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur!!!");
            alert.setHeaderText("Avertissement Erreur!!!");
            alert.setContentText("votre compte de commission n'est pas activee");
            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

}
