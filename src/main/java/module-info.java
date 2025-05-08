module com.b2la.dbroffice {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.prefs;

    opens com.b2la.dbroffice to javafx.fxml;
    exports com.b2la.dbroffice;
    exports com.b2la.dbroffice.controller;
    opens com.b2la.dbroffice.controller to javafx.fxml;
    opens com.b2la.dbroffice.dao to com.google.gson;

}