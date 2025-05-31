package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.preference.Storage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private Label username, countAdmin, countOffice, countAgents, countClients, countUsers,
            countSend, countWithdrawal, countDeposit, countFactory,
            amountSendUSD, amountWithdrawalUSD, amountDepositUSD,
            amountSendCDF, amountWithdrawalCDF, amountDepositCDF;
    private Button btnTaux;
    private TextField searchTaux;
    private TableView tableTaux;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    protected void onClose() {
        Storage.removeLogin();
        System.exit(0);
    }
}
