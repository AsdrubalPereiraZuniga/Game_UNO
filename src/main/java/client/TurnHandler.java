package client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class TurnHandler {

    private static Label lblCurrentTurn;    

    public static void setLabel(Label label) {
        lblCurrentTurn = label;
    }       

    public static void updateTurn(String username) {
        if (lblCurrentTurn != null) {
            Platform.runLater(() -> {
                lblCurrentTurn.setText("Turno actual: " + username);
            });
        }
    }        
}
