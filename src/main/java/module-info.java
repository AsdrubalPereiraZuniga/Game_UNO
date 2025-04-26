module com.mycompany.game_uno_so {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires javafx.media;

    opens com.mycompany.game_uno_so to javafx.fxml;
    opens controllers to javafx.fxml;
    
    exports com.mycompany.game_uno_so;
    exports controllers;
}
