module com.mycompany.game_uno_so {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.game_uno_so to javafx.fxml;
    exports com.mycompany.game_uno_so;
}
