package com.b2la.dbroffice;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1000, screenBounds.getWidth() * 0.92);
        double height = Math.min(620, screenBounds.getHeight() * 0.90);
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("DBR Core Finance");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setMinWidth(Math.min(420, width));
        stage.setMinHeight(Math.min(500, height));
        stage.setMaxWidth(screenBounds.getWidth());
        stage.setMaxHeight(screenBounds.getHeight());
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
