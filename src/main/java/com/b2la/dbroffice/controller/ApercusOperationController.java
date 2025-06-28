package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Operation;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

public class ApercusOperationController {

    @FXML
    Label title;

    public void affichage(Operation op){
        title.setText(String.format(
                """
                   Opération
                   - Réf : %s
                   - Montant : %.2f %s
                   - Commission : %.2f %s
                   - Expéditeur : %s %s 
                   - Phone Exp: %s
                   - Bénéficiaire : %s %s 
                   - Phone Ben: %s
                   - Date : %s
                   - Statut : %s
        
                """,
                op.getCodereference(),
                op.getAmount(), op.getDevice(),
                op.getCommission(), op.getDevice(),
                op.getExp().getUser().getSurname(),
                op.getExp().getUser().getFirstname(),// Suppose que Accounts a une méthode getName()
                op.getExp().getUser().getPhone(),
                op.getBen().getUser().getSurname(),   // Pareil ici
                op.getBen().getUser().getFirstname(),
                op.getBen().getUser().getPhone(),
                op.getDateoperation(),
                op.getState().getLibelle()       // Suppose que State est un enum ou a une méthode name()
        ));
    }



}
