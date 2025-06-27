package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Operation;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

public class ApercusOperationController {
    @FXML
    private TableView<Operation> operationTable;
    @FXML private TableColumn<Operation, String> colDate;
    @FXML private TableColumn<Operation, String> colType;
    @FXML private TableColumn<Operation, String> colMontant;
    @FXML private TableColumn<Operation, String> colExpBen;
    @FXML private TableColumn<Operation, String> colStatut;

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().format(formatter))
        );

        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(getTypeOperation(cellData.getValue()))
        );

        colMontant.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f USD", cellData.getValue().getMontant()))
        );

        colExpBen.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getExp().getNom() + " ➡ " + cellData.getValue().getBen().getNom())
        );

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(getEmojiStatut(statut));
                    label.setStyle("-fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        });

        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut())
        );
    }

}
