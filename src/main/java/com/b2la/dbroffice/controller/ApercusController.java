package com.b2la.dbroffice.controller;

import com.b2la.dbroffice.dao.Cost;
import com.b2la.dbroffice.dao.StreamUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

public class ApercusController {

    @FXML private ComboBox comboType;
    @FXML private DatePicker fieldDate;
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList(
                "CLIENT", "AGENT", "OFFICIEL", "ADMIN"
        ));
        comboType.setDisable(true);
        fieldDate.setDisable(true);
    }


    public void typeOpera(StreamUser su){
        id=cost.getId();
        fieldPercent.setText(String.valueOf(cost.getPercent()));
        fieldMax.setText(String.valueOf(cost.getMax()));
        fieldMin.setText(String.valueOf(cost.getMin()));
        devise.setValue(String.valueOf(cost.getDevices()));
        typeOperation=true;

    }
}
